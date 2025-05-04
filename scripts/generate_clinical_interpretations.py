#!/usr/bin/env python3
"""
Clinical Interpretation Generator for Oncology Genomics Pipeline

This script generates clinical interpretations for variants in the oncology genomics pipeline
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
    "interpretations": "/interpretations",
    "variants": "/variants"
}

# Clinical significance levels
CLINICAL_SIGNIFICANCE = ["Pathogenic", "Likely Pathogenic", "Uncertain Significance", "Likely Benign", "Benign"]

# Evidence levels
EVIDENCE_LEVELS = ["Strong", "Moderate", "Limited", "Supporting"]

# Clinical databases
CLINICAL_DATABASES = ["ClinVar", "COSMIC", "OncoKB", "CIViC", "Cancer Genome Interpreter"]

# Therapeutic implications
THERAPEUTIC_IMPLICATIONS = {
    "EGFR p.L858R": {
        "drugs": ["Osimertinib", "Erlotinib", "Gefitinib", "Afatinib"],
        "sensitivity": "Sensitive",
        "evidence": "Level A - Validated association"
    },
    "EGFR p.T790M": {
        "drugs": ["Osimertinib"],
        "sensitivity": "Sensitive",
        "evidence": "Level A - Validated association"
    },
    "BRAF p.V600E": {
        "drugs": ["Dabrafenib + Trametinib", "Vemurafenib + Cobimetinib", "Encorafenib + Binimetinib"],
        "sensitivity": "Sensitive",
        "evidence": "Level A - Validated association"
    },
    "KRAS p.G12C": {
        "drugs": ["Sotorasib", "Adagrasib"],
        "sensitivity": "Sensitive",
        "evidence": "Level A - Validated association"
    },
    "KRAS p.G12D": {
        "drugs": ["MEK inhibitors"],
        "sensitivity": "Potentially Sensitive",
        "evidence": "Level C - Clinical evidence"
    },
    "BRCA1 p.Q1756fs": {
        "drugs": ["Olaparib", "Niraparib", "Rucaparib"],
        "sensitivity": "Sensitive",
        "evidence": "Level A - Validated association"
    },
    "BRCA2 p.N372H": {
        "drugs": ["Olaparib", "Niraparib", "Rucaparib"],
        "sensitivity": "Potentially Sensitive",
        "evidence": "Level C - Clinical evidence"
    },
    "PIK3CA p.E545K": {
        "drugs": ["Alpelisib"],
        "sensitivity": "Sensitive",
        "evidence": "Level A - Validated association"
    }
}

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

def load_variants():
    """Load variants from file"""
    try:
        with open("data/variants.json", "r") as f:
            return json.load(f)
    except FileNotFoundError:
        print("Variants file not found. Please run generate_variants.py first.")
        return []
    except Exception as e:
        print(f"Error loading variants: {str(e)}")
        return []

def generate_clinical_interpretations(variants):
    """Generate clinical interpretations for variants"""
    print(f"Generating clinical interpretations for {len(variants)} variants...")
    interpretations = []
    error_count = 0
    
    for i, variant in enumerate(variants):
        try:
            # Get variant info
            gene = variant.get("gene")
            hgvs = variant.get("hgvs")
            variant_id = variant.get("variantId")
            variant_key = f"{gene} {hgvs}"
            
            # Determine clinical significance based on variant info
            if "variant_info" in variant and "pathogenicity" in variant["variant_info"]:
                clinical_significance = variant["variant_info"]["pathogenicity"]
            else:
                # Assign based on gene and hgvs
                if variant_key in THERAPEUTIC_IMPLICATIONS:
                    clinical_significance = "Pathogenic"
                elif gene in ["EGFR", "BRAF", "KRAS", "BRCA1", "BRCA2", "PIK3CA", "TP53", "APC"]:
                    clinical_significance = random.choices(
                        CLINICAL_SIGNIFICANCE[:3],  # Pathogenic, Likely Pathogenic, Uncertain Significance
                        weights=[0.6, 0.3, 0.1]
                    )[0]
                else:
                    clinical_significance = random.choices(
                        CLINICAL_SIGNIFICANCE,
                        weights=[0.2, 0.2, 0.3, 0.2, 0.1]
                    )[0]
            
            # Determine evidence level based on clinical significance
            if clinical_significance == "Pathogenic":
                evidence_level = random.choices(EVIDENCE_LEVELS, weights=[0.6, 0.3, 0.1, 0.0])[0]
            elif clinical_significance == "Likely Pathogenic":
                evidence_level = random.choices(EVIDENCE_LEVELS, weights=[0.3, 0.5, 0.2, 0.0])[0]
            elif clinical_significance == "Uncertain Significance":
                evidence_level = random.choices(EVIDENCE_LEVELS, weights=[0.0, 0.2, 0.5, 0.3])[0]
            else:  # Benign or Likely Benign
                evidence_level = random.choices(EVIDENCE_LEVELS, weights=[0.0, 0.1, 0.3, 0.6])[0]
            
            # Generate interpretation text
            if variant_key in THERAPEUTIC_IMPLICATIONS:
                implications = THERAPEUTIC_IMPLICATIONS[variant_key]
                drugs = ", ".join(implications["drugs"])
                interpretation_text = (
                    f"This variant is {clinical_significance.lower()} and is associated with response to {drugs}. "
                    f"Patients with this variant are typically {implications['sensitivity'].lower()} to these therapies. "
                    f"Evidence: {implications['evidence']}."
                )
            elif clinical_significance in ["Pathogenic", "Likely Pathogenic"]:
                interpretation_text = (
                    f"This variant is {clinical_significance.lower()} and may contribute to cancer development or progression. "
                    f"It affects the {gene} gene, which is involved in {random.choice(['cell growth', 'DNA repair', 'cell signaling', 'apoptosis'])}. "
                    f"This finding may have implications for prognosis and treatment selection."
                )
            elif clinical_significance == "Uncertain Significance":
                interpretation_text = (
                    f"This variant is of uncertain significance. Current evidence is insufficient to determine its role in cancer. "
                    f"Additional studies may be needed to clarify its clinical impact."
                )
            else:  # Benign or Likely Benign
                interpretation_text = (
                    f"This variant is {clinical_significance.lower()} and is not expected to contribute to cancer development or progression. "
                    f"It represents a normal variation in the {gene} gene."
                )
            
            # Generate citations
            num_citations = random.randint(1, 3)
            citations = [f"PMID:{random.randint(20000000, 36000000)}" for _ in range(num_citations)]
            citations_text = ", ".join(citations)
            
            # Generate source databases
            num_databases = random.randint(1, 3)
            databases = random.sample(CLINICAL_DATABASES, num_databases)
            databases_text = ", ".join(databases)
            
            # Create interpretation data
            interpretation_data = {
                "clinicalSignificance": clinical_significance,
                "evidenceLevel": evidence_level,
                "interpretationText": interpretation_text,
                "citations": citations_text,
                "sourceDatabases": databases_text,
                "lastUpdated": datetime.now().strftime("%Y-%m-%d")
            }
            
            # Create interpretation via API
            response = call_api("POST", "interpretations", interpretation_data, 
                               resource_id=f"variant/{variant_id}", 
                               description=f"Create interpretation for variant {gene} {hgvs}", 
                               use_params=False)
            
            if response:
                # Add variant info to response for reference
                response["variant"] = {
                    "variantId": variant_id,
                    "gene": gene,
                    "hgvs": hgvs
                }
                interpretations.append(response)
                print(f"Created interpretation for variant {gene} {hgvs}")
            else:
                print(f"Failed to create interpretation for variant {gene} {hgvs}")
                error_count += 1
        except Exception as e:
            print(f"Error generating interpretation for variant {variant.get('gene')} {variant.get('hgvs')}: {str(e)}")
            error_count += 1
    
    print(f"Generated {len(interpretations)} clinical interpretations with {error_count} errors.")
    return interpretations

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
    parser = argparse.ArgumentParser(description="Generate clinical interpretations for oncology genomics")
    parser.add_argument("--url", help="Base URL for the API", default="http://localhost:8080/oncology")
    
    args = parser.parse_args()
    
    global BASE_URL
    BASE_URL = args.url
    
    # Wait for application to be ready
    if not wait_for_application():
        return 1
    
    # Ensure data directory exists
    os.makedirs("data", exist_ok=True)
    
    # Load variants
    variants = load_variants()
    if not variants:
        return 1
    
    # Generate clinical interpretations
    interpretations = generate_clinical_interpretations(variants)
    
    # Save interpretations to file for reference
    with open("data/interpretations.json", "w") as f:
        json.dump(interpretations, f, indent=2)
    
    print(f"Generated {len(interpretations)} clinical interpretations.")
    print(f"Interpretation data saved to data/interpretations.json")
    
    return 0

if __name__ == "__main__":
    exit(main())
