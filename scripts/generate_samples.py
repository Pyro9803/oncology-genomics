#!/usr/bin/env python3
"""
Sample Data Generator for Oncology Genomics Pipeline

This script generates realistic sample data for the oncology genomics pipeline
and populates the database with it.
"""

import requests
import json
import random
from datetime import datetime, timedelta
import argparse
import os

# API configuration
BASE_URL = "http://localhost:8080/oncology"
TIMEOUT = 10  # seconds

# API paths
API_PATHS = {
    "patients": "/patients",
    "diagnoses": "/diagnoses",
    "samples": "/samples"
}

# Sample types
SAMPLE_TYPES = ["Tumor", "Normal", "Metastasis", "Recurrence"]

# Tissue types
TISSUE_TYPES = ["FFPE", "Fresh Frozen", "Blood", "Bone Marrow", "Fine Needle Aspirate"]

# Sample sites
SAMPLE_SITES = ["Primary", "Metastatic", "Lymph Node", "Circulating"]

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

def load_diagnoses():
    """Load diagnoses from file"""
    try:
        with open("data/diagnoses.json", "r") as f:
            return json.load(f)
    except FileNotFoundError:
        print("Diagnoses file not found. Please run generate_diagnoses.py first.")
        return []
    except Exception as e:
        print(f"Error loading diagnoses: {str(e)}")
        return []

def generate_samples(diagnoses):
    """Generate and create samples for diagnoses"""
    print(f"Generating samples for {len(diagnoses)} diagnoses...")
    samples = []
    error_count = 0
    
    for i, diagnosis in enumerate(diagnoses):
        try:
            # Determine number of samples for this diagnosis (1-3)
            num_samples = random.randint(1, 3)
            
            # Always create at least one tumor sample
            sample_types = ["Tumor"]
            
            # Add additional sample types if needed
            if num_samples > 1:
                additional_types = random.sample(SAMPLE_TYPES[1:], num_samples - 1)
                sample_types.extend(additional_types)
            
            for j, sample_type in enumerate(sample_types):
                # Generate collection date (within 30 days after diagnosis date)
                diagnosis_date = datetime.strptime(diagnosis["diagnosisDate"], "%Y-%m-%d")
                collection_date = (diagnosis_date + timedelta(days=random.randint(0, 30))).strftime("%Y-%m-%d")
                
                # Select tissue type based on sample type
                if sample_type == "Tumor" or sample_type == "Metastasis" or sample_type == "Recurrence":
                    tissue_type = random.choice(TISSUE_TYPES[:2])  # FFPE or Fresh Frozen
                elif sample_type == "Normal":
                    tissue_type = random.choice(TISSUE_TYPES[2:])  # Blood, Bone Marrow, or Fine Needle Aspirate
                else:
                    tissue_type = random.choice(TISSUE_TYPES)
                
                # Select sample site based on sample type
                if sample_type == "Tumor":
                    sample_site = "Primary"
                elif sample_type == "Metastasis":
                    sample_site = "Metastatic"
                elif sample_type == "Normal":
                    sample_site = random.choice(["Blood", "Bone Marrow"])
                else:
                    sample_site = random.choice(SAMPLE_SITES)
                
                # Generate tumor purity for tumor samples
                tumor_purity = random.randint(60, 95) if sample_type in ["Tumor", "Metastasis", "Recurrence"] else None
                
                # Create sample data
                sample_data = {
                    "diagnosisId": diagnosis["diagnosisId"],
                    "sampleType": sample_type,
                    "collectionDate": collection_date,
                    "tissueType": tissue_type,
                    "sampleSite": sample_site,
                    "tumorPurity": tumor_purity,
                    "notes": f"{sample_type} sample collected for genomic analysis."
                }
                
                # Create sample via API
                response = call_api("POST", "samples", sample_data, 
                                   description=f"Create {sample_type} sample for diagnosis {i+1}/{len(diagnoses)}", use_params=False)
                
                if response:
                    samples.append(response)
                    print(f"Created {sample_type} sample for diagnosis {diagnosis.get('diagnosisId')}")
                else:
                    print(f"Failed to create {sample_type} sample for diagnosis {diagnosis.get('diagnosisId')}")
                    error_count += 1
        except Exception as e:
            print(f"Error generating sample for diagnosis {diagnosis.get('diagnosisId')}: {str(e)}")
            error_count += 1
    
    print(f"Generated {len(samples)} samples with {error_count} errors.")
    return samples

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
        import time
        time.sleep(5)
    
    print("Application is not available after maximum retries")
    return False

def main():
    """Main function"""
    parser = argparse.ArgumentParser(description="Generate sample data for oncology genomics")
    parser.add_argument("--url", help="Base URL for the API", default="http://localhost:8080/oncology")
    
    args = parser.parse_args()
    
    global BASE_URL
    BASE_URL = args.url
    
    # Wait for application to be ready
    if not wait_for_application():
        return 1
    
    # Ensure data directory exists
    os.makedirs("data", exist_ok=True)
    
    # Load diagnoses
    diagnoses = load_diagnoses()
    if not diagnoses:
        return 1
    
    # Generate samples
    samples = generate_samples(diagnoses)
    
    # Save samples to file for reference
    with open("data/samples.json", "w") as f:
        json.dump(samples, f, indent=2)
    
    print(f"Generated {len(samples)} samples.")
    print(f"Sample data saved to data/samples.json")
    
    return 0

if __name__ == "__main__":
    exit(main())
