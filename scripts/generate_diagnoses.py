#!/usr/bin/env python3
"""
Diagnosis Data Generator for Oncology Genomics Pipeline

This script generates realistic diagnosis data for the oncology genomics pipeline
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
    "patients": "/patients"
    # Diagnoses are created through the patients endpoint
}

# Cancer types with appropriate staging
CANCER_TYPES = [
    {
        "name": "Non-Small Cell Lung Cancer",
        "histologies": ["Adenocarcinoma", "Squamous Cell Carcinoma", "Large Cell Carcinoma"],
        "common_mutations": ["EGFR", "ALK", "ROS1", "KRAS", "BRAF", "MET", "RET", "NTRK"]
    },
    {
        "name": "Breast Cancer",
        "histologies": ["Invasive Ductal Carcinoma", "Invasive Lobular Carcinoma", "Triple Negative"],
        "common_mutations": ["BRCA1", "BRCA2", "PIK3CA", "TP53", "PTEN", "CDH1", "PALB2"]
    },
    {
        "name": "Colorectal Cancer",
        "histologies": ["Adenocarcinoma", "Mucinous Adenocarcinoma", "Signet Ring Cell Carcinoma"],
        "common_mutations": ["KRAS", "NRAS", "BRAF", "PIK3CA", "APC", "TP53", "SMAD4"]
    },
    {
        "name": "Melanoma",
        "histologies": ["Superficial Spreading", "Nodular", "Lentigo Maligna", "Acral Lentiginous"],
        "common_mutations": ["BRAF", "NRAS", "KIT", "PTEN", "TP53", "CDKN2A"]
    },
    {
        "name": "Ovarian Cancer",
        "histologies": ["High-grade Serous", "Low-grade Serous", "Clear Cell", "Endometrioid"],
        "common_mutations": ["BRCA1", "BRCA2", "TP53", "PTEN", "PIK3CA", "ARID1A"]
    }
]

# Primary sites by cancer type
PRIMARY_SITES = {
    "Non-Small Cell Lung Cancer": ["Right Upper Lobe", "Right Middle Lobe", "Right Lower Lobe", "Left Upper Lobe", "Left Lower Lobe"],
    "Breast Cancer": ["Right Breast", "Left Breast", "Right Axillary Tail", "Left Axillary Tail"],
    "Colorectal Cancer": ["Cecum", "Ascending Colon", "Transverse Colon", "Descending Colon", "Sigmoid Colon", "Rectum"],
    "Melanoma": ["Head and Neck", "Trunk", "Upper Extremity", "Lower Extremity", "Acral"],
    "Ovarian Cancer": ["Right Ovary", "Left Ovary", "Bilateral Ovaries", "Fallopian Tube"]
}

# Metastasis sites by cancer type
METASTASIS_SITES = {
    "Non-Small Cell Lung Cancer": ["Brain", "Bone", "Liver", "Adrenal Gland", "Contralateral Lung"],
    "Breast Cancer": ["Bone", "Liver", "Lung", "Brain", "Lymph Nodes"],
    "Colorectal Cancer": ["Liver", "Lung", "Peritoneum", "Lymph Nodes", "Ovary"],
    "Melanoma": ["Lung", "Liver", "Brain", "Bone", "Distant Skin/Subcutaneous Tissue"],
    "Ovarian Cancer": ["Peritoneum", "Liver", "Lung", "Lymph Nodes", "Pleura"]
}

def call_api(method, resource_type, data=None, resource_id=None, description="API Call", use_params=True):
    """Make an API call and return the response"""
    url = f"{BASE_URL}{API_PATHS[resource_type]}"
    
    if resource_id:
        url += f"/{resource_id}"
    
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

def load_patients():
    """Load patients from file"""
    try:
        with open("data/patients.json", "r") as f:
            return json.load(f)
    except FileNotFoundError:
        print("Patients file not found. Please run generate_patients.py first.")
        return []
    except Exception as e:
        print(f"Error loading patients: {str(e)}")
        return []

def generate_diagnoses(patients):
    """Generate and create diagnoses for patients"""
    print(f"Generating diagnoses for {len(patients)} patients...")
    diagnoses = []
    error_count = 0
    
    for i, patient in enumerate(patients):
        try:
            # Determine number of diagnoses for this patient (1-2)
            num_diagnoses = random.choices([1, 2], weights=[0.8, 0.2])[0]
            patient_id = patient["patientId"]
            
            for j in range(num_diagnoses):
                # Select random cancer type
                cancer_type_info = random.choice(CANCER_TYPES)
                cancer_type = cancer_type_info["name"]
                
                # Select histology
                histology = random.choice(cancer_type_info["histologies"])
                
                # Parse TNM staging
                t_stage = random.choice(["T1", "T2", "T3", "T4"])
                n_stage = random.choice(["N0", "N1", "N2", "N3"])
                m_stage = random.choice(["M0", "M1"]) if random.random() < 0.2 else "M0"  # 20% chance of metastatic disease
                
                # Generate diagnosis date (within last 2 years)
                diagnosis_date = (datetime.now() - timedelta(days=random.randint(1, 730))).strftime("%Y-%m-%d")
                
                # Create diagnosis data with parameters matching the controller
                diagnosis_data = {
                    "cancerType": cancer_type,
                    "histology": histology,
                    "tStage": t_stage,
                    "nStage": n_stage,
                    "mStage": m_stage,
                    "diagnosisDate": diagnosis_date
                }
                
                # Create diagnosis via API - using the patient/{patientId}/diagnoses endpoint
                response = call_api("POST", "patients", diagnosis_data, 
                                   resource_id=f"{patient_id}/diagnoses",
                                   description=f"Create diagnosis for patient {i+1}/{len(patients)}", 
                                   use_params=True)  # Using params since controller uses @RequestParam
                
                if response:
                    diagnoses.append(response)
                    print(f"Created diagnosis for patient {patient.get('firstName')} {patient.get('lastName')}: {cancer_type}")
                else:
                    print(f"Failed to create diagnosis for patient {patient.get('patientId')}")
                    error_count += 1
        except Exception as e:
            print(f"Error generating diagnosis for patient {patient.get('patientId')}: {str(e)}")
            error_count += 1
    
    print(f"Generated {len(diagnoses)} diagnoses with {error_count} errors.")
    return diagnoses

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
    parser = argparse.ArgumentParser(description="Generate diagnosis data for oncology genomics")
    parser.add_argument("--url", help="Base URL for the API", default="http://localhost:8080/oncology")
    
    args = parser.parse_args()
    
    global BASE_URL
    BASE_URL = args.url
    
    # Wait for application to be ready
    if not wait_for_application():
        return 1
    
    # Ensure data directory exists
    os.makedirs("data", exist_ok=True)
    
    # Load patients
    patients = load_patients()
    if not patients:
        return 1
    
    # Generate diagnoses
    diagnoses = generate_diagnoses(patients)
    
    # Save diagnoses to file for reference
    with open("data/diagnoses.json", "w") as f:
        json.dump(diagnoses, f, indent=2)
    
    print(f"Generated {len(diagnoses)} diagnoses.")
    print(f"Diagnosis data saved to data/diagnoses.json")
    
    return 0

if __name__ == "__main__":
    exit(main())
