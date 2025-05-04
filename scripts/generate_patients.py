#!/usr/bin/env python3
"""
Patient Data Generator for Oncology Genomics Pipeline

This script generates realistic patient data for the oncology genomics pipeline
and populates the database with it.
"""

import requests
import json
import random
from datetime import datetime, timedelta
from faker import Faker
import argparse

# Initialize Faker
fake = Faker()

# API configuration
BASE_URL = "http://localhost:8080/oncology"
TIMEOUT = 10  # seconds

# API paths
API_PATHS = {
    "patients": "/patients"
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

def generate_patients(count):
    """Generate and create patients"""
    print(f"Generating {count} patients...")
    patients = []
    error_count = 0
    
    for i in range(count):
        try:
            # Generate patient data
            gender = random.choice(["Male", "Female"])
            first_name = fake.first_name_male() if gender == "Male" else fake.first_name_female()
            
            # Create patient data with correct parameter names
            patient_data = {
                "medicalRecordNumber": f"MRN-{random.randint(10000, 99999)}",
                "firstName": first_name,
                "lastName": fake.last_name(),
                "dateOfBirth": (datetime.now() - timedelta(days=365 * random.randint(30, 80))).strftime("%Y-%m-%d"),
                "gender": gender,
                "contactNumber": fake.phone_number(),  # Changed from contactPhone to contactNumber
                "email": fake.email(),               # Changed from contactEmail to email
                "address": fake.address().replace('\n', ', ')
            }
            
            # Create patient via API - using params=True since the API uses @RequestParam
            response = call_api("POST", "patients", patient_data, 
                               description=f"Create patient {i+1}/{count}", use_params=True)
            
            if response:
                patients.append(response)
                print(f"Created patient {i+1}/{count}: {response.get('firstName')} {response.get('lastName')}")
            else:
                print(f"Failed to create patient {i+1}/{count}")
                error_count += 1
        except Exception as e:
            print(f"Error generating patient: {str(e)}")
            error_count += 1
    
    print(f"Generated {len(patients)} patients with {error_count} errors.")
    return patients

def wait_for_application():
    """Wait for the application to be ready"""
    max_retries = 10
    retry_count = 0
    
    print("Checking if application is ready...")
    
    while retry_count < max_retries:
        try:
            response = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
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
    parser = argparse.ArgumentParser(description="Generate patient data for oncology genomics")
    parser.add_argument("--count", type=int, default=10, help="Number of patients to generate")
    parser.add_argument("--url", help="Base URL for the API", default="http://localhost:8080/oncology")
    
    args = parser.parse_args()
    
    global BASE_URL
    BASE_URL = args.url
    
    # Wait for application to be ready
    if not wait_for_application():
        return 1
    
    # Generate patients
    patients = generate_patients(args.count)
    
    # Save patients to file for reference
    with open("data/patients.json", "w") as f:
        json.dump(patients, f, indent=2)
    
    print(f"Generated {len(patients)} patients.")
    print(f"Patient data saved to data/patients.json")
    
    return 0

if __name__ == "__main__":
    exit(main())
