#!/bin/bash
# Simple script to generate sample data for the oncology genomics application

BASE_URL="http://localhost:8080/oncology/api"
echo "Generating sample data for Oncology Genomics application"
echo "========================================================"

# Function to make API calls
call_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo "Creating: $description"
    
    if [ -n "$data" ]; then
        response=$(curl -s -X $method -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    else
        response=$(curl -s -X $method "$BASE_URL$endpoint")
    fi
    
    echo "Response: $response"
    echo "----------------------------------------"
    
    # Extract ID from response if it's a JSON object with an ID field
    if [[ "$response" == *"id"* ]]; then
        id=$(echo $response | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
        echo $id
    fi
}

# Create patients
create_patient() {
    local mrn=$1
    local first_name=$2
    local last_name=$3
    local dob=$4
    local gender=$5
    
    call_api "POST" "/patients?medicalRecordNumber=$mrn&firstName=$first_name&lastName=$last_name&dateOfBirth=$dob&gender=$gender" "" "Patient $first_name $last_name"
}

# Create diagnoses
create_diagnosis() {
    local patient_id=$1
    local cancer_type=$2
    local stage=$3
    local diagnosis_date=$4
    
    call_api "POST" "/patients/$patient_id/diagnoses?cancerType=$cancer_type&stageGroup=$stage&diagnosisDate=$diagnosis_date" "" "Diagnosis for patient $patient_id"
}

# Create samples
create_sample() {
    local patient_id=$1
    local diagnosis_id=$2
    local sample_type=$3
    local collection_date=$4
    
    call_api "POST" "/samples?patientId=$patient_id&diagnosisId=$diagnosis_id&sampleType=$sample_type&collectionDate=$collection_date" "" "Sample for patient $patient_id"
}

echo "Creating patients..."
patient1_id=$(create_patient "MRN12345" "John" "Doe" "1965-05-15" "Male")
patient2_id=$(create_patient "MRN67890" "Jane" "Smith" "1972-08-22" "Female")
patient3_id=$(create_patient "MRN54321" "Robert" "Johnson" "1958-11-03" "Male")

echo "Creating diagnoses..."
diagnosis1_id=$(create_diagnosis "$patient1_id" "Non-Small Cell Lung Cancer" "Stage IIIA" "2024-01-15")
diagnosis2_id=$(create_diagnosis "$patient2_id" "Breast Cancer" "Stage II" "2024-02-20")
diagnosis3_id=$(create_diagnosis "$patient3_id" "Colorectal Cancer" "Stage III" "2024-03-10")

echo "Creating samples..."
tumor_sample1_id=$(create_sample "$patient1_id" "$diagnosis1_id" "Tumor" "2024-01-20")
normal_sample1_id=$(create_sample "$patient1_id" "$diagnosis1_id" "Normal" "2024-01-20")
tumor_sample2_id=$(create_sample "$patient2_id" "$diagnosis2_id" "Tumor" "2024-02-25")
normal_sample2_id=$(create_sample "$patient2_id" "$diagnosis2_id" "Normal" "2024-02-25")
tumor_sample3_id=$(create_sample "$patient3_id" "$diagnosis3_id" "Tumor" "2024-03-15")
normal_sample3_id=$(create_sample "$patient3_id" "$diagnosis3_id" "Normal" "2024-03-15")

echo "Sample data generation complete!"
echo "You can now access the application at http://localhost:8080/oncology/api"
