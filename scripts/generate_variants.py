#!/usr/bin/env python3
"""
Variant Generator for Oncology Genomics Pipeline

This script generates somatic variants for GATK analysis jobs in the oncology genomics pipeline
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
    "variants": "/variants",
    "gatk": "/analysis"
}

# Known cancer-associated variants for testing
CANCER_VARIANTS = [
    {
        "gene": "EGFR",
        "hgvs": "p.L858R",
        "chromosome": "7",
        "position": 55259515,
        "reference": "T",
        "alternate": "G",
        "cancer_type": "Non-Small Cell Lung Cancer",
        "pathogenicity": "Pathogenic",
        "variant_effect": "Missense",
        "expected_diagnosis": True
    },
    {
        "gene": "EGFR",
        "hgvs": "p.T790M",
        "chromosome": "7",
        "position": 55249071,
        "reference": "C",
        "alternate": "T",
        "cancer_type": "Non-Small Cell Lung Cancer",
        "pathogenicity": "Pathogenic",
        "variant_effect": "Missense",
        "expected_diagnosis": True
    },
    {
        "gene": "BRAF",
        "hgvs": "p.V600E",
        "chromosome": "7",
        "position": 140453136,
        "reference": "A",
        "alternate": "T",
        "cancer_type": "Melanoma",
        "pathogenicity": "Pathogenic",
        "variant_effect": "Missense",
        "expected_diagnosis": True
    },
    {
        "gene": "KRAS",
        "hgvs": "p.G12D",
        "chromosome": "12",
        "position": 25398284,
        "reference": "G",
        "alternate": "A",
        "cancer_type": "Colorectal Cancer",
        "pathogenicity": "Pathogenic",
        "variant_effect": "Missense",
        "expected_diagnosis": True
    },
    {
        "gene": "KRAS",
        "hgvs": "p.G12C",
        "chromosome": "12",
        "position": 25398285,
        "reference": "C",
        "alternate": "A",
        "cancer_type": "Non-Small Cell Lung Cancer",
        "pathogenicity": "Pathogenic",
        "variant_effect": "Missense",
        "expected_diagnosis": True
    },
    {
        "gene": "TP53",
        "hgvs": "p.R175H",
        "chromosome": "17",
        "position": 7578406,
        "reference": "G",
        "alternate": "A",
        "cancer_type": "Multiple Cancers",
        "pathogenicity": "Pathogenic",
        "variant_effect": "Missense",
        "expected_diagnosis": True
    },
    {
        "gene": "BRCA1",
        "hgvs": "p.Q1756fs",
        "chromosome": "17",
        "position": 41245466,
        "reference": "G",
        "alternate": "GA",
        "cancer_type": "Breast/Ovarian Cancer",
        "pathogenicity": "Pathogenic",
        "variant_effect": "Frameshift",
        "expected_diagnosis": True
    },
    {
        "gene": "BRCA2",
        "hgvs": "p.N372H",
        "chromosome": "13",
        "position": 32906729,
        "reference": "A",
        "alternate": "C",
        "cancer_type": "Breast/Ovarian Cancer",
        "pathogenicity": "Pathogenic",
        "variant_effect": "Missense",
        "expected_diagnosis": True
    },
    {
        "gene": "PIK3CA",
        "hgvs": "p.E545K",
        "chromosome": "3",
        "position": 178936091,
        "reference": "G",
        "alternate": "A",
        "cancer_type": "Breast Cancer",
        "pathogenicity": "Pathogenic",
        "variant_effect": "Missense",
        "expected_diagnosis": True
    },
    {
        "gene": "APC",
        "hgvs": "p.R1450X",
        "chromosome": "5",
        "position": 112175240,
        "reference": "C",
        "alternate": "T",
        "cancer_type": "Colorectal Cancer",
        "pathogenicity": "Pathogenic",
        "variant_effect": "Nonsense",
        "expected_diagnosis": True
    },
    # Control cases - benign variants
    {
        "gene": "EGFR",
        "hgvs": "p.Q787Q",
        "chromosome": "7",
        "position": 55249063,
        "reference": "G",
        "alternate": "A",
        "cancer_type": "None",
        "pathogenicity": "Benign",
        "variant_effect": "Synonymous",
        "expected_diagnosis": False
    },
    {
        "gene": "TP53",
        "hgvs": "p.P72R",
        "chromosome": "17",
        "position": 7579472,
        "reference": "G",
        "alternate": "C",
        "cancer_type": "None",
        "pathogenicity": "Benign",
        "variant_effect": "Missense",
        "expected_diagnosis": False
    }
]

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

def load_gatk_jobs():
    """Load GATK jobs from file"""
    try:
        with open("data/gatk_jobs.json", "r") as f:
            return json.load(f)
    except FileNotFoundError:
        print("GATK jobs file not found. Please run generate_gatk_analysis.py first.")
        return []
    except Exception as e:
        print(f"Error loading GATK jobs: {str(e)}")
        return []

def generate_variants(gatk_jobs):
    """Generate somatic variants for GATK jobs"""
    print(f"Generating variants for {len(gatk_jobs)} GATK jobs...")
    variants = []
    error_count = 0
    
    # Filter for completed or in-progress jobs
    valid_jobs = [job for job in gatk_jobs if job.get("status") in ["COMPLETED", "IN_PROGRESS", "PENDING"]]
    
    if not valid_jobs:
        print("No valid GATK jobs found for variant generation.")
        return variants
    
    print(f"Found {len(valid_jobs)} valid GATK jobs for variant generation.")
    
    for i, job in enumerate(valid_jobs):
        try:
            # Determine number of variants for this job (2-5)
            num_variants = random.randint(2, 5)
            
            # Select random variants from our list
            selected_variants = random.sample(CANCER_VARIANTS, num_variants)
            
            for j, variant_info in enumerate(selected_variants):
                # Generate allele frequency (0.05-0.5 for pathogenic, 0.4-0.6 for benign)
                if variant_info["pathogenicity"] == "Pathogenic":
                    allele_frequency = random.uniform(0.05, 0.5)
                else:
                    allele_frequency = random.uniform(0.4, 0.6)
                
                # Generate read depth (100-1000)
                read_depth = random.randint(100, 1000)
                
                # Create variant data
                variant_data = {
                    "gene": variant_info["gene"],
                    "hgvs": variant_info["hgvs"],
                    "chromosome": variant_info["chromosome"],
                    "position": variant_info["position"],
                    "referenceAllele": variant_info["reference"],
                    "alternateAllele": variant_info["alternate"],
                    "variantType": "SNV" if len(variant_info["reference"]) == 1 and len(variant_info["alternate"]) == 1 else "INDEL",
                    "variantEffect": variant_info["variant_effect"],
                    "alleleFrequency": allele_frequency,
                    "readDepth": read_depth,
                    "cosmicId": f"COSM{random.randint(100000, 999999)}",
                    "dbsnpId": f"rs{random.randint(100000, 999999)}"
                }
                
                # Create variant via API
                response = call_api("POST", "variants", variant_data, 
                                   resource_id=f"variant-calling/{job['variantCallingId']}", 
                                   description=f"Create variant {variant_info['gene']} {variant_info['hgvs']} for job {i+1}/{len(valid_jobs)}", 
                                   use_params=False)
                
                if response:
                    # Add job and variant info to response for reference
                    response["job"] = {
                        "variantCallingId": job["variantCallingId"],
                        "status": job["status"]
                    }
                    response["variant_info"] = variant_info
                    variants.append(response)
                    print(f"Created variant {variant_info['gene']} {variant_info['hgvs']} for job {job['variantCallingId']}")
                else:
                    print(f"Failed to create variant {variant_info['gene']} {variant_info['hgvs']} for job {job['variantCallingId']}")
                    error_count += 1
        except Exception as e:
            print(f"Error generating variants for job {job.get('variantCallingId')}: {str(e)}")
            error_count += 1
    
    print(f"Generated {len(variants)} variants with {error_count} errors.")
    return variants

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
    parser = argparse.ArgumentParser(description="Generate variants for oncology genomics")
    parser.add_argument("--url", help="Base URL for the API", default="http://localhost:8080/oncology")
    
    args = parser.parse_args()
    
    global BASE_URL
    BASE_URL = args.url
    
    # Wait for application to be ready
    if not wait_for_application():
        return 1
    
    # Ensure data directory exists
    os.makedirs("data", exist_ok=True)
    
    # Load GATK jobs
    gatk_jobs = load_gatk_jobs()
    if not gatk_jobs:
        return 1
    
    # Generate variants
    variants = generate_variants(gatk_jobs)
    
    # Save variants to file for reference
    with open("data/variants.json", "w") as f:
        json.dump(variants, f, indent=2)
    
    print(f"Generated {len(variants)} variants.")
    print(f"Variant data saved to data/variants.json")
    
    return 0

if __name__ == "__main__":
    exit(main())
