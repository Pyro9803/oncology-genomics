#!/usr/bin/env python3
"""
Sequencing Data Generator for Oncology Genomics Pipeline

This script generates realistic sequencing data for samples in the oncology genomics pipeline
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
    "samples": "/samples",
    "sequencing": "/sequencing"
}

# Sequencing platforms
PLATFORMS = ["Illumina NovaSeq 6000", "Illumina NextSeq 550", "Illumina MiSeq", "Ion Torrent", "PacBio"]

# Library preparation kits
LIBRARY_PREP_KITS = ["TruSeq DNA PCR-Free", "Nextera DNA Flex", "SureSelect Human All Exon V7", "IDT xGen Exome Research Panel", "Twist Human Core Exome"]

# Sequencing types
SEQUENCING_TYPES = ["WGS", "WES", "Panel", "RNA-Seq"]

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

def generate_sequencing_data(samples):
    """Generate and create sequencing data for samples"""
    print(f"Generating sequencing data for {len(samples)} samples...")
    sequencing_data_list = []
    error_count = 0
    
    for i, sample in enumerate(samples):
        try:
            # Select platform
            platform = random.choice(PLATFORMS)
            
            # Select library prep kit
            library_prep_kit = random.choice(LIBRARY_PREP_KITS)
            
            # Select sequencing type based on sample type
            if sample.get("sampleType") == "Tumor" or sample.get("sampleType") == "Metastasis":
                # Tumor samples typically get WES or targeted panel
                sequencing_type = random.choices(SEQUENCING_TYPES[:3], weights=[0.2, 0.5, 0.3])[0]
            elif sample.get("sampleType") == "Normal":
                # Normal samples typically get WGS or WES
                sequencing_type = random.choices(SEQUENCING_TYPES[:2], weights=[0.3, 0.7])[0]
            else:
                sequencing_type = random.choice(SEQUENCING_TYPES)
            
            # Set target coverage based on sequencing type
            if sequencing_type == "WGS":
                target_coverage = random.randint(30, 50)
            elif sequencing_type == "WES":
                target_coverage = random.randint(100, 200)
            elif sequencing_type == "Panel":
                target_coverage = random.randint(500, 1000)
            else:  # RNA-Seq
                target_coverage = random.randint(50, 100)
            
            # Generate mean coverage (slightly variable from target)
            mean_coverage_value = target_coverage * random.uniform(0.9, 1.1)
            
            # Generate sequencing date (within 7 days after sample collection)
            collection_date = datetime.strptime(sample["collectionDate"], "%Y-%m-%d")
            sequencing_date = (collection_date + timedelta(days=random.randint(1, 7))).strftime("%Y-%m-%d")
            
            # Create sequencing data
            sequencing_data = {
                "platform": platform,
                "libraryPrepKit": library_prep_kit,
                "sequencingType": sequencing_type,
                "targetCoverage": target_coverage,
                "meanCoverageValue": mean_coverage_value,
                "sequencingDate": sequencing_date
            }
            
            # Create sequencing data via API
            response = call_api("POST", "samples", sequencing_data, resource_id=f"{sample['sampleId']}/sequencing", 
                               description=f"Add sequencing data for sample {i+1}/{len(samples)}", use_params=True)
            
            if response:
                # Add sample info to response for reference
                response["sample"] = {
                    "sampleId": sample["sampleId"],
                    "sampleType": sample["sampleType"]
                }
                sequencing_data_list.append(response)
                print(f"Added sequencing data for sample {sample.get('sampleId')}")
            else:
                print(f"Failed to add sequencing data for sample {sample.get('sampleId')}")
                error_count += 1
        except Exception as e:
            print(f"Error generating sequencing data for sample {sample.get('sampleId')}: {str(e)}")
            error_count += 1
    
    print(f"Generated {len(sequencing_data_list)} sequencing data records with {error_count} errors.")
    return sequencing_data_list

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
    parser = argparse.ArgumentParser(description="Generate sequencing data for oncology genomics")
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
    
    # Generate sequencing data
    sequencing_data_list = generate_sequencing_data(samples)
    
    # Save sequencing data to file for reference
    with open("data/sequencing_data.json", "w") as f:
        json.dump(sequencing_data_list, f, indent=2)
    
    print(f"Generated {len(sequencing_data_list)} sequencing data records.")
    print(f"Sequencing data saved to data/sequencing_data.json")
    
    return 0

if __name__ == "__main__":
    exit(main())
