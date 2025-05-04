#!/usr/bin/env python3
"""
GATK Analysis Generator for Oncology Genomics Pipeline

This script generates GATK analysis jobs for samples in the oncology genomics pipeline
and populates the database with it.
"""

import requests
import json
import random
from datetime import datetime, timedelta
import argparse
import os
import time

# API configuration
BASE_URL = "http://localhost:8080/oncology"
TIMEOUT = 10  # seconds

# API paths
API_PATHS = {
    "samples": "/samples",
    "gatk": "/analysis"
}

# GATK pipeline versions
PIPELINE_VERSIONS = ["4.2.0.0", "4.1.9.0", "4.1.8.1", "4.1.7.0"]

# Variant calling methods
CALLING_METHODS = ["Mutect2", "HaplotypeCaller", "Strelka2", "Varscan2"]

def call_api(method, resource_type, data=None, resource_id=None, subresource_type=None, subresource_id=None, description="API Call", use_params=True):
    """Make an API call and return the response"""
    url = f"{BASE_URL}{API_PATHS[resource_type]}"
    
    if resource_id:
        url += f"/{resource_id}"
        
    if subresource_type:
        url += f"/{subresource_type}"
        
    if subresource_id:
        url += f"/{subresource_id}"
    
    headers = {"Content-Type": "application/json"}
    
    print(f"Making API call: {description}")
    print(f"URL: {url}")
    
    try:
        if method == "GET":
            if data and use_params:
                response = requests.get(url, params=data, timeout=TIMEOUT)
            else:
                response = requests.get(url, timeout=TIMEOUT)
        elif method == "POST":
            if data:
                if use_params:
                    response = requests.post(url, params=data, timeout=TIMEOUT)
                else:
                    response = requests.post(url, json=data, headers=headers, timeout=TIMEOUT)
            else:
                response = requests.post(url, timeout=TIMEOUT)
        elif method == "PUT":
            response = requests.put(url, json=data, headers=headers, timeout=TIMEOUT)
        elif method == "DELETE":
            response = requests.delete(url, timeout=TIMEOUT)
        else:
            print(f"Unsupported method: {method}")
            return None
        
        if response.status_code >= 200 and response.status_code < 300:
            try:
                return response.json()
            except:
                print(f"Response is not JSON: {response.text}")
                return {"success": True}
        else:
            print(f"API call failed with status code {response.status_code}: {response.text}")
            return None
    except Exception as e:
        print(f"Error making API call: {str(e)}")
        return None

def load_samples():
    """Load samples from file"""
    try:
        with open("data/samples.json", "r") as f:
            return json.load(f)
    except FileNotFoundError:
        print("Samples file not found. Please run generate_samples.py first.")
        return []
    except Exception as e:
        print(f"Error loading samples: {str(e)}")
        return []

def find_normal_sample(samples, tumor_sample):
    """Find a matching normal sample for a tumor sample"""
    # Get diagnosis ID from tumor sample
    diagnosis_id = tumor_sample.get("diagnosis", {}).get("diagnosisId")
    if not diagnosis_id:
        return None
    
    # Find normal samples with the same diagnosis ID
    normal_samples = [
        s for s in samples 
        if s.get("sampleType") == "Normal" and 
        s.get("diagnosis", {}).get("diagnosisId") == diagnosis_id
    ]
    
    if normal_samples:
        return random.choice(normal_samples)
    return None

def generate_gatk_jobs(samples):
    """Generate and submit GATK jobs for tumor-normal pairs"""
    print("Generating GATK analysis jobs...")
    gatk_jobs = []
    error_count = 0
    
    # Filter for tumor samples
    tumor_samples = [s for s in samples if s.get("sampleType") in ["Tumor", "Metastasis", "Recurrence"]]
    
    print(f"Found {len(tumor_samples)} tumor samples for GATK analysis.")
    
    for i, tumor_sample in enumerate(tumor_samples):
        try:
            # Find matching normal sample if available
            normal_sample = find_normal_sample(samples, tumor_sample)
            
            # Select pipeline version and calling method
            pipeline_version = random.choice(PIPELINE_VERSIONS)
            calling_method = random.choice(CALLING_METHODS)
            
            # Create job data
            job_data = {
                "tumorSampleId": tumor_sample["sampleId"],
                "pipelineVersion": pipeline_version,
                "callingMethod": calling_method
            }
            
            # Add normal sample if available
            if normal_sample:
                job_data["normalSampleId"] = normal_sample["sampleId"]
                print(f"Using tumor-normal pair: Tumor {tumor_sample['sampleId']} - Normal {normal_sample['sampleId']}")
            else:
                print(f"Using tumor-only analysis for sample {tumor_sample['sampleId']}")
            
            # Submit GATK job via API
            response = call_api("POST", "gatk", job_data, resource_id="submit", 
                               description=f"Submit GATK job for tumor sample {i+1}/{len(tumor_samples)}", use_params=True)
            
            if response:
                gatk_jobs.append(response)
                print(f"Submitted GATK job for tumor sample {tumor_sample.get('sampleId')}")
                
                # Check job status after a short delay
                time.sleep(2)
                job_status = call_api("GET", "gatk", resource_id=f"{response['variantCallingId']}/status", 
                                    description=f"Check status of GATK job {response['variantCallingId']}")
                
                if job_status:
                    print(f"GATK job status: {job_status.get('status')}")
                    # Update the job in our list with the status
                    for job in gatk_jobs:
                        if job['variantCallingId'] == response['variantCallingId']:
                            job['status'] = job_status.get('status')
            else:
                print(f"Failed to submit GATK job for tumor sample {tumor_sample.get('sampleId')}")
                error_count += 1
        except Exception as e:
            print(f"Error generating GATK job for tumor sample {tumor_sample.get('sampleId')}: {str(e)}")
            error_count += 1
    
    print(f"Generated {len(gatk_jobs)} GATK jobs with {error_count} errors.")
    return gatk_jobs

def wait_for_application():
    """Wait for the application to be ready"""
    max_retries = 10
    retry_count = 0
    
    print("Checking if application is ready...")
    
    while retry_count < max_retries:
        try:
            response = requests.get(f"{BASE_URL}/health", timeout=5)
            if response.status_code == 200:
                print("Application is ready!")
                return True
        except:
            pass
        
        retry_count += 1
        print(f"Application not ready, retrying in 5 seconds... ({retry_count}/{max_retries})")
        time.sleep(5)
    
    print("Application is not available after maximum retries")
    return False

def main():
    """Main function"""
    parser = argparse.ArgumentParser(description="Generate GATK analysis jobs for oncology genomics")
    parser.add_argument("--url", help="Base URL for the API", default="http://localhost:8080/oncology")
    
    args = parser.parse_args()
    
    global BASE_URL
    BASE_URL = args.url
    
    # Wait for application to be ready
    if not wait_for_application():
        return 1
    
    # Ensure data directory exists
    os.makedirs("data", exist_ok=True)
    
    # Load samples
    samples = load_samples()
    if not samples:
        return 1
    
    # Generate GATK jobs
    gatk_jobs = generate_gatk_jobs(samples)
    
    # Save GATK jobs to file for reference
    with open("data/gatk_jobs.json", "w") as f:
        json.dump(gatk_jobs, f, indent=2)
    
    print(f"Generated {len(gatk_jobs)} GATK jobs.")
    print(f"GATK job data saved to data/gatk_jobs.json")
    
    return 0

if __name__ == "__main__":
    exit(main())
