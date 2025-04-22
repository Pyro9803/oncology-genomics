#!/bin/bash
# Test script for Oncology Genomics Pipeline API

# Set base URL
BASE_URL="http://localhost:8080"
echo "Testing Oncology Genomics Pipeline API at $BASE_URL"
echo "=================================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to make API calls and display results
call_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo -e "${BLUE}Testing: $description${NC}"
    echo "Endpoint: $method $endpoint"
    
    if [ -n "$data" ]; then
        echo "Request data: $data"
        response=$(curl -s -X $method -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    else
        response=$(curl -s -X $method "$BASE_URL$endpoint")
    fi
    
    if [ -n "$response" ]; then
        echo -e "${GREEN}Response: $response${NC}"
        echo "Test passed!"
    else
        echo -e "${RED}No response or error occurred${NC}"
        echo "Test failed!"
    fi
    echo "=================================================="
}

# Wait for the application to be ready
echo "Waiting for the application to be ready..."
until $(curl --output /dev/null --silent --head --fail $BASE_URL/actuator/health); do
    printf '.'
    sleep 5
done
echo -e "${GREEN}Application is ready!${NC}"
echo "=================================================="

# 1. Patient Management Tests
echo -e "${BLUE}PATIENT MANAGEMENT TESTS${NC}"

# 1.1 Create a new patient
call_api "POST" "/patients" '{
    "medicalRecordNumber": "MRN12345",
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1980-01-15",
    "gender": "Male",
    "contactNumber": "555-123-4567",
    "email": "john.doe@example.com",
    "address": "123 Main St, Boston, MA 02115"
}' "Create a new patient"

# 1.2 Create a second patient
call_api "POST" "/patients" '{
    "medicalRecordNumber": "MRN67890",
    "firstName": "Jane",
    "lastName": "Smith",
    "dateOfBirth": "1975-05-20",
    "gender": "Female",
    "contactNumber": "555-987-6543",
    "email": "jane.smith@example.com",
    "address": "456 Oak Ave, Boston, MA 02116"
}' "Create a second patient"

# 1.3 Get all patients
call_api "GET" "/patients" "" "Get all patients"

# 1.4 Get patient by ID (assuming ID 1)
call_api "GET" "/patients/1" "" "Get patient by ID"

# 1.5 Update patient
call_api "PUT" "/patients/1?contactNumber=555-111-2222&email=john.updated@example.com" "" "Update patient contact information"

# 2. Diagnosis Management Tests
echo -e "${BLUE}DIAGNOSIS MANAGEMENT TESTS${NC}"

# 2.1 Add diagnosis to patient
call_api "POST" "/patients/1/diagnoses" '{
    "cancerType": "Non-Small Cell Lung Cancer",
    "diagnosisDate": "2024-01-10",
    "tStage": "T2",
    "nStage": "N1",
    "mStage": "M0",
    "histology": "Adenocarcinoma",
    "notes": "Initial diagnosis following chest CT and biopsy"
}' "Add diagnosis to patient"

# 2.2 Add second diagnosis to patient
call_api "POST" "/patients/1/diagnoses" '{
    "cancerType": "Colorectal Cancer",
    "diagnosisDate": "2024-03-15",
    "tStage": "T3",
    "nStage": "N1",
    "mStage": "M0",
    "histology": "Adenocarcinoma",
    "notes": "Secondary malignancy discovered during follow-up"
}' "Add second diagnosis to patient"

# 2.3 Get diagnoses for patient
call_api "GET" "/patients/1/diagnoses" "" "Get diagnoses for patient"

# 3. Sample Management Tests
echo -e "${BLUE}SAMPLE MANAGEMENT TESTS${NC}"

# 3.1 Add sample to diagnosis
call_api "POST" "/diagnoses/1/samples" '{
    "sampleType": "Tumor Tissue",
    "collectionDate": "2024-01-12",
    "tumorPurity": 0.75,
    "sampleQualityScore": 8.5,
    "storageLocation": "Freezer A, Shelf 3, Box 12"
}' "Add sample to diagnosis"

# 3.2 Add second sample to diagnosis
call_api "POST" "/diagnoses/1/samples" '{
    "sampleType": "Blood",
    "collectionDate": "2024-01-12",
    "tumorPurity": 0.0,
    "sampleQualityScore": 9.2,
    "storageLocation": "Freezer B, Shelf 1, Box 5"
}' "Add second sample to diagnosis (blood for germline)"

# 3.3 Get samples for diagnosis
call_api "GET" "/diagnoses/1/samples" "" "Get samples for diagnosis"

# 3.4 Get samples for patient
call_api "GET" "/patients/1/samples" "" "Get samples for patient"

# 4. Sequencing Data Management Tests
echo -e "${BLUE}SEQUENCING DATA MANAGEMENT TESTS${NC}"

# 4.1 Add sequencing data to sample
call_api "POST" "/samples/1/sequencing" '{
    "platform": "Illumina NovaSeq 6000",
    "libraryPrep": "TruSeq DNA PCR-Free",
    "sequencingDate": "2024-01-20",
    "readLength": 150,
    "coverage": 100,
    "fastqPath": "/data/fastq/sample1_tumor"
}' "Add sequencing data to tumor sample"

# 4.2 Add sequencing data to normal sample
call_api "POST" "/samples/2/sequencing" '{
    "platform": "Illumina NovaSeq 6000",
    "libraryPrep": "TruSeq DNA PCR-Free",
    "sequencingDate": "2024-01-20",
    "readLength": 150,
    "coverage": 50,
    "fastqPath": "/data/fastq/sample1_normal"
}' "Add sequencing data to normal sample"

# 4.3 Get sequencing data for sample
call_api "GET" "/samples/1/sequencing" "" "Get sequencing data for sample"

# 5. GATK Analysis Tests
echo -e "${BLUE}GATK ANALYSIS TESTS${NC}"

# 5.1 Submit GATK job for tumor-normal pair
call_api "POST" "/gatk/submit" '{
    "tumorSampleId": 1,
    "normalSampleId": 2,
    "analysisType": "Somatic",
    "parameters": {
        "caller": "Mutect2",
        "reference": "hg38",
        "filterSettings": "default"
    }
}' "Submit GATK job for tumor-normal pair"

# 5.2 Get GATK job status (assuming job ID 1)
call_api "GET" "/gatk/status/1" "" "Get GATK job status"

# 6. Variant Management Tests
echo -e "${BLUE}VARIANT MANAGEMENT TESTS${NC}"

# 6.1 Get variants by gene
call_api "GET" "/variants/gene/EGFR" "" "Get variants in EGFR gene"

# 6.2 Get variants by chromosome region
call_api "GET" "/variants/region?chromosome=7&start=55000000&end=55500000" "" "Get variants in chromosome 7 region"

# 6.3 Get filtered variants
call_api "GET" "/variants/filtered/PASS" "" "Get variants with PASS filter status"

# 7. Clinical Interpretation Tests
echo -e "${BLUE}CLINICAL INTERPRETATION TESTS${NC}"

# 7.1 Add clinical interpretation for a variant
call_api "POST" "/interpretations" '{
    "variantId": 1,
    "clinicalSignificance": "Pathogenic",
    "evidenceLevel": "Strong",
    "interpretation": "EGFR L858R mutation is associated with response to EGFR TKIs",
    "references": "PMID:12345678, PMID:87654321"
}' "Add clinical interpretation for a variant"

# 7.2 Get interpretations for a variant
call_api "GET" "/interpretations/variant/1" "" "Get interpretations for a variant"

# 8. Therapy Recommendation Tests
echo -e "${BLUE}THERAPY RECOMMENDATION TESTS${NC}"

# 8.1 Add therapy recommendation
call_api "POST" "/therapies" '{
    "patientId": 1,
    "diagnosisId": 1,
    "drugName": "Osimertinib",
    "dosage": "80mg daily",
    "recommendationDate": "2024-02-01",
    "evidenceLevel": "Level 1A",
    "variantIds": [1],
    "notes": "First-line therapy for EGFR-mutated NSCLC"
}' "Add therapy recommendation"

# 8.2 Get therapy recommendations for patient
call_api "GET" "/therapies/patient/1" "" "Get therapy recommendations for patient"

# 9. Follow-up Management Tests
echo -e "${BLUE}FOLLOW-UP MANAGEMENT TESTS${NC}"

# 9.1 Add follow-up record
call_api "POST" "/followups" '{
    "patientId": 1,
    "followUpDate": "2024-03-01",
    "clinicalStatus": "Stable Disease",
    "imagingResults": "30% reduction in tumor size",
    "adverseEvents": "Grade 1 rash, Grade 2 diarrhea",
    "nextFollowUpDate": "2024-06-01",
    "notes": "Continue current therapy"
}' "Add follow-up record"

# 9.2 Get follow-up records for patient
call_api "GET" "/followups/patient/1" "" "Get follow-up records for patient"

echo -e "${GREEN}All tests completed!${NC}"
