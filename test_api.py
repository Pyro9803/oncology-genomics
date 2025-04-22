#!/usr/bin/env python3
"""
Test script for Oncology Genomics Pipeline API
This script provides a more structured way to test the API with better error handling
"""

import requests
import json
import time
import sys
from datetime import datetime, date
from colorama import init, Fore, Style

# Initialize colorama
init()

# Configuration
BASE_URL = "http://localhost:8080"
TIMEOUT = 10  # seconds
MAX_RETRIES = 5
RETRY_DELAY = 5  # seconds

# Test data storage
test_data = {
    "patients": [],
    "diagnoses": [],
    "samples": [],
    "sequencing_data": [],
    "gatk_jobs": [],
    "variants": [],
    "interpretations": [],
    "therapies": [],
    "followups": []
}

def print_header(message):
    """Print a header message"""
    print(f"\n{Fore.BLUE}{Style.BRIGHT}" + "=" * 80)
    print(f" {message}")
    print("=" * 80 + f"{Style.RESET_ALL}\n")

def print_success(message):
    """Print a success message"""
    print(f"{Fore.GREEN}{message}{Style.RESET_ALL}")

def print_error(message):
    """Print an error message"""
    print(f"{Fore.RED}{message}{Style.RESET_ALL}")

def print_info(message):
    """Print an info message"""
    print(f"{Fore.CYAN}{message}{Style.RESET_ALL}")

def call_api(method, endpoint, data=None, description="API Call"):
    """Make an API call and return the response"""
    url = f"{BASE_URL}{endpoint}"
    headers = {"Content-Type": "application/json"}
    
    print_info(f"Testing: {description}")
    print(f"Endpoint: {method} {url}")
    
    if data:
        print(f"Request data: {json.dumps(data, indent=2)}")
    
    try:
        if method.upper() == "GET":
            response = requests.get(url, timeout=TIMEOUT)
        elif method.upper() == "POST":
            response = requests.post(url, json=data, headers=headers, timeout=TIMEOUT)
        elif method.upper() == "PUT":
            response = requests.put(url, json=data, headers=headers, timeout=TIMEOUT)
        elif method.upper() == "DELETE":
            response = requests.delete(url, timeout=TIMEOUT)
        else:
            print_error(f"Unsupported method: {method}")
            return None
        
        response.raise_for_status()
        
        if response.status_code == 204:  # No content
            print_success("Response: No content (204)")
            print_success("Test passed!")
            return None
        
        response_data = response.json()
        print_success(f"Response: {json.dumps(response_data, indent=2)}")
        print_success("Test passed!")
        return response_data
    
    except requests.exceptions.RequestException as e:
        print_error(f"Error: {str(e)}")
        print_error("Test failed!")
        return None

def wait_for_application():
    """Wait for the application to be ready"""
    print_info("Waiting for the application to be ready...")
    
    for i in range(MAX_RETRIES):
        try:
            response = requests.get(f"{BASE_URL}/actuator/health", timeout=TIMEOUT)
            if response.status_code == 200:
                print_success("Application is ready!")
                return True
        except requests.exceptions.RequestException:
            pass
        
        print(".", end="", flush=True)
        time.sleep(RETRY_DELAY)
    
    print_error("\nApplication did not become ready in time.")
    return False

def test_patient_management():
    """Test patient management endpoints"""
    print_header("PATIENT MANAGEMENT TESTS")
    
    # Create patients
    patients = [
        {
            "medicalRecordNumber": "MRN12345",
            "firstName": "John",
            "lastName": "Doe",
            "dateOfBirth": "1980-01-15",
            "gender": "Male",
            "contactNumber": "555-123-4567",
            "email": "john.doe@example.com",
            "address": "123 Main St, Boston, MA 02115"
        },
        {
            "medicalRecordNumber": "MRN67890",
            "firstName": "Jane",
            "lastName": "Smith",
            "dateOfBirth": "1975-05-20",
            "gender": "Female",
            "contactNumber": "555-987-6543",
            "email": "jane.smith@example.com",
            "address": "456 Oak Ave, Boston, MA 02116"
        }
    ]
    
    for i, patient_data in enumerate(patients):
        response = call_api("POST", "/patients", patient_data, f"Create patient {i+1}")
        if response:
            test_data["patients"].append(response)
    
    # Get all patients
    call_api("GET", "/patients", description="Get all patients")
    
    # Get patient by ID
    if test_data["patients"]:
        patient_id = test_data["patients"][0]["patientId"]
        call_api("GET", f"/patients/{patient_id}", description=f"Get patient by ID {patient_id}")
    
    # Update patient
    if test_data["patients"]:
        patient_id = test_data["patients"][0]["patientId"]
        call_api("PUT", f"/patients/{patient_id}?contactNumber=555-111-2222&email=john.updated@example.com", 
                description=f"Update patient {patient_id}")

def test_diagnosis_management():
    """Test diagnosis management endpoints"""
    print_header("DIAGNOSIS MANAGEMENT TESTS")
    
    if not test_data["patients"]:
        print_error("No patients available for testing diagnoses")
        return
    
    patient_id = test_data["patients"][0]["patientId"]
    
    # Add diagnoses
    diagnoses = [
        {
            "cancerType": "Non-Small Cell Lung Cancer",
            "diagnosisDate": "2024-01-10",
            "tStage": "T2",
            "nStage": "N1",
            "mStage": "M0",
            "histology": "Adenocarcinoma",
            "notes": "Initial diagnosis following chest CT and biopsy"
        },
        {
            "cancerType": "Colorectal Cancer",
            "diagnosisDate": "2024-03-15",
            "tStage": "T3",
            "nStage": "N1",
            "mStage": "M0",
            "histology": "Adenocarcinoma",
            "notes": "Secondary malignancy discovered during follow-up"
        }
    ]
    
    for i, diagnosis_data in enumerate(diagnoses):
        response = call_api("POST", f"/patients/{patient_id}/diagnoses", diagnosis_data, 
                          f"Add diagnosis {i+1} to patient {patient_id}")
        if response:
            test_data["diagnoses"].append(response)
    
    # Get diagnoses for patient
    call_api("GET", f"/patients/{patient_id}/diagnoses", description=f"Get diagnoses for patient {patient_id}")

def test_sample_management():
    """Test sample management endpoints"""
    print_header("SAMPLE MANAGEMENT TESTS")
    
    if not test_data["diagnoses"]:
        print_error("No diagnoses available for testing samples")
        return
    
    diagnosis_id = test_data["diagnoses"][0]["diagnosisId"]
    patient_id = test_data["patients"][0]["patientId"]
    
    # Add samples
    samples = [
        {
            "sampleType": "Tumor Tissue",
            "collectionDate": "2024-01-12",
            "tumorPurity": 0.75,
            "sampleQualityScore": 8.5,
            "storageLocation": "Freezer A, Shelf 3, Box 12"
        },
        {
            "sampleType": "Blood",
            "collectionDate": "2024-01-12",
            "tumorPurity": 0.0,
            "sampleQualityScore": 9.2,
            "storageLocation": "Freezer B, Shelf 1, Box 5"
        }
    ]
    
    for i, sample_data in enumerate(samples):
        response = call_api("POST", f"/diagnoses/{diagnosis_id}/samples", sample_data, 
                          f"Add sample {i+1} to diagnosis {diagnosis_id}")
        if response:
            test_data["samples"].append(response)
    
    # Get samples for diagnosis
    call_api("GET", f"/diagnoses/{diagnosis_id}/samples", 
           description=f"Get samples for diagnosis {diagnosis_id}")
    
    # Get samples for patient
    call_api("GET", f"/patients/{patient_id}/samples", 
           description=f"Get samples for patient {patient_id}")

def test_sequencing_data():
    """Test sequencing data endpoints"""
    print_header("SEQUENCING DATA MANAGEMENT TESTS")
    
    if not test_data["samples"] or len(test_data["samples"]) < 2:
        print_error("Not enough samples available for testing sequencing data")
        return
    
    tumor_sample_id = test_data["samples"][0]["sampleId"]
    normal_sample_id = test_data["samples"][1]["sampleId"]
    
    # Add sequencing data
    sequencing_data = [
        {
            "platform": "Illumina NovaSeq 6000",
            "libraryPrep": "TruSeq DNA PCR-Free",
            "sequencingDate": "2024-01-20",
            "readLength": 150,
            "coverage": 100,
            "fastqPath": "/data/fastq/sample1_tumor"
        },
        {
            "platform": "Illumina NovaSeq 6000",
            "libraryPrep": "TruSeq DNA PCR-Free",
            "sequencingDate": "2024-01-20",
            "readLength": 150,
            "coverage": 50,
            "fastqPath": "/data/fastq/sample1_normal"
        }
    ]
    
    response = call_api("POST", f"/samples/{tumor_sample_id}/sequencing", sequencing_data[0], 
                       f"Add sequencing data to tumor sample {tumor_sample_id}")
    if response:
        test_data["sequencing_data"].append(response)
    
    response = call_api("POST", f"/samples/{normal_sample_id}/sequencing", sequencing_data[1], 
                       f"Add sequencing data to normal sample {normal_sample_id}")
    if response:
        test_data["sequencing_data"].append(response)
    
    # Get sequencing data for sample
    call_api("GET", f"/samples/{tumor_sample_id}/sequencing", 
           description=f"Get sequencing data for sample {tumor_sample_id}")

def test_gatk_analysis():
    """Test GATK analysis endpoints"""
    print_header("GATK ANALYSIS TESTS")
    
    if not test_data["samples"] or len(test_data["samples"]) < 2:
        print_error("Not enough samples available for testing GATK analysis")
        return
    
    tumor_sample_id = test_data["samples"][0]["sampleId"]
    normal_sample_id = test_data["samples"][1]["sampleId"]
    
    # Submit GATK job
    gatk_job_data = {
        "tumorSampleId": tumor_sample_id,
        "normalSampleId": normal_sample_id,
        "analysisType": "Somatic",
        "parameters": {
            "caller": "Mutect2",
            "reference": "hg38",
            "filterSettings": "default"
        }
    }
    
    response = call_api("POST", "/gatk/submit", gatk_job_data, 
                       "Submit GATK job for tumor-normal pair")
    if response:
        test_data["gatk_jobs"].append(response)
        
        # Get GATK job status
        job_id = response["jobId"]
        call_api("GET", f"/gatk/status/{job_id}", 
               description=f"Get GATK job status for job {job_id}")

def test_variant_management():
    """Test variant management endpoints"""
    print_header("VARIANT MANAGEMENT TESTS")
    
    # Get variants by gene
    call_api("GET", "/variants/gene/EGFR", description="Get variants in EGFR gene")
    
    # Get variants by chromosome region
    call_api("GET", "/variants/region?chromosome=7&start=55000000&end=55500000", 
           description="Get variants in chromosome 7 region")
    
    # Get filtered variants
    call_api("GET", "/variants/filtered/PASS", description="Get variants with PASS filter status")

def test_clinical_interpretation():
    """Test clinical interpretation endpoints"""
    print_header("CLINICAL INTERPRETATION TESTS")
    
    # For testing purposes, assume variant ID 1 exists
    variant_id = 1
    
    # Add clinical interpretation
    interpretation_data = {
        "variantId": variant_id,
        "clinicalSignificance": "Pathogenic",
        "evidenceLevel": "Strong",
        "interpretation": "EGFR L858R mutation is associated with response to EGFR TKIs",
        "references": "PMID:12345678, PMID:87654321"
    }
    
    response = call_api("POST", "/interpretations", interpretation_data, 
                       f"Add clinical interpretation for variant {variant_id}")
    if response:
        test_data["interpretations"].append(response)
    
    # Get interpretations for variant
    call_api("GET", f"/interpretations/variant/{variant_id}", 
           description=f"Get interpretations for variant {variant_id}")

def test_therapy_recommendation():
    """Test therapy recommendation endpoints"""
    print_header("THERAPY RECOMMENDATION TESTS")
    
    if not test_data["patients"] or not test_data["diagnoses"]:
        print_error("No patients or diagnoses available for testing therapy recommendations")
        return
    
    patient_id = test_data["patients"][0]["patientId"]
    diagnosis_id = test_data["diagnoses"][0]["diagnosisId"]
    
    # Add therapy recommendation
    therapy_data = {
        "patientId": patient_id,
        "diagnosisId": diagnosis_id,
        "drugName": "Osimertinib",
        "dosage": "80mg daily",
        "recommendationDate": "2024-02-01",
        "evidenceLevel": "Level 1A",
        "variantIds": [1],  # Assume variant ID 1 exists
        "notes": "First-line therapy for EGFR-mutated NSCLC"
    }
    
    response = call_api("POST", "/therapies", therapy_data, 
                       f"Add therapy recommendation for patient {patient_id}")
    if response:
        test_data["therapies"].append(response)
    
    # Get therapy recommendations for patient
    call_api("GET", f"/therapies/patient/{patient_id}", 
           description=f"Get therapy recommendations for patient {patient_id}")

def test_followup_management():
    """Test follow-up management endpoints"""
    print_header("FOLLOW-UP MANAGEMENT TESTS")
    
    if not test_data["patients"]:
        print_error("No patients available for testing follow-ups")
        return
    
    patient_id = test_data["patients"][0]["patientId"]
    
    # Add follow-up record
    followup_data = {
        "patientId": patient_id,
        "followUpDate": "2024-03-01",
        "clinicalStatus": "Stable Disease",
        "imagingResults": "30% reduction in tumor size",
        "adverseEvents": "Grade 1 rash, Grade 2 diarrhea",
        "nextFollowUpDate": "2024-06-01",
        "notes": "Continue current therapy"
    }
    
    response = call_api("POST", "/followups", followup_data, 
                       f"Add follow-up record for patient {patient_id}")
    if response:
        test_data["followups"].append(response)
    
    # Get follow-up records for patient
    call_api("GET", f"/followups/patient/{patient_id}", 
           description=f"Get follow-up records for patient {patient_id}")

def main():
    """Main function to run all tests"""
    print_header("ONCOLOGY GENOMICS PIPELINE API TESTS")
    print(f"Base URL: {BASE_URL}")
    print(f"Timestamp: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    if not wait_for_application():
        sys.exit(1)
    
    # Run all tests
    test_patient_management()
    test_diagnosis_management()
    test_sample_management()
    test_sequencing_data()
    test_gatk_analysis()
    test_variant_management()
    test_clinical_interpretation()
    test_therapy_recommendation()
    test_followup_management()
    
    print_header("TEST SUMMARY")
    print_success("All tests completed!")

if __name__ == "__main__":
    main()
