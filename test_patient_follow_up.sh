#!/bin/bash
# Script to test the patient treatment and follow-up functionality

BASE_URL="http://localhost:8080/oncology"
echo "Testing Patient Treatment and Follow-up Functionality"
echo "===================================================="

# Wait for the application to start
echo "Waiting for the application to start..."
sleep 5

# 1. Get existing patient and therapy recommendation
echo "Getting existing patient and therapy recommendation..."
patient_id=1

# First, let's check if we have clinical interpretations
echo "Checking for clinical interpretations..."
curl -s "$BASE_URL/clinical/interpretations"

# Try different endpoints to find therapy recommendations
echo "Trying to find therapy recommendations..."
therapy_id=$(curl -s "$BASE_URL/clinical/interpretations/1/therapy" | grep -o '"therapyId":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -z "$therapy_id" ]; then
    echo "Trying alternative endpoint..."
    therapy_id=$(curl -s "$BASE_URL/clinical/therapies" | grep -o '"therapyId":[0-9]*' | grep -o '[0-9]*' | head -1)
fi

if [ -z "$therapy_id" ]; then
    echo "Creating a test therapy recommendation..."
    THERAPY_URL="/clinical/interpretations/1/therapy"
    THERAPY_PARAMS="therapyName=Gefitinib&therapyType=EGFR%20TKI"
    THERAPY_PARAMS="$THERAPY_PARAMS&evidenceLevel=Level%201A"
    THERAPY_PARAMS="$THERAPY_PARAMS&evidenceSource=NCCN%20Guidelines"
    THERAPY_PARAMS="$THERAPY_PARAMS&referenceId=PMID12345678"
    THERAPY_PARAMS="$THERAPY_PARAMS&recommendationSummary=First-line%20therapy%20for%20EGFR-mutant%20NSCLC"
    THERAPY_PARAMS="$THERAPY_PARAMS&contraindications=None"
    
    therapy_id=$(curl -s -X POST "$BASE_URL$THERAPY_URL?$THERAPY_PARAMS" | grep -o '"therapyId":[0-9]*' | grep -o '[0-9]*' | head -1)
fi

if [ -z "$therapy_id" ]; then
    echo "No therapy recommendation found. Please run the clinical interpretation test first."
    exit 1
fi

echo "Using patient ID: $patient_id and therapy ID: $therapy_id"

# 2. Create initial follow-up record (therapy started)
echo "Creating initial follow-up record (therapy started)..."

FOLLOW_UP_URL="/patient-follow-ups"
FOLLOW_UP_PARAMS="patientId=$patient_id&therapyId=$therapy_id"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&followUpDate=$(date +%Y-%m-%d)"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&clinicalStatus=STABLE"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&responseAssessment=STABLE_DISEASE"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&imagingResults=Baseline%20imaging%20shows%20tumor%20size%20of%204.2cm"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&laboratoryResults=Normal%20liver%20and%20kidney%20function"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&performanceStatus=ECOG%201"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&clinicalNotes=Patient%20started%20on%20Gefitinib%20therapy%20today.%20Tolerated%20first%20dose%20well."
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&recordedBy=Dr.%20Smith"

initial_follow_up_id=$(curl -s -X POST "$BASE_URL$FOLLOW_UP_URL?$FOLLOW_UP_PARAMS" | grep -o '"followUpId":[0-9]*' | grep -o '[0-9]*' | head -1)

echo "Initial follow-up ID: $initial_follow_up_id"

# 3. Create 1-month follow-up record (partial response)
echo "Creating 1-month follow-up record (partial response)..."

# Set the date to one month from now
one_month_date=$(date -d "1 month" +%Y-%m-%d)

FOLLOW_UP_PARAMS="patientId=$patient_id&therapyId=$therapy_id"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&followUpDate=$one_month_date"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&clinicalStatus=IMPROVED"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&responseAssessment=PARTIAL_RESPONSE"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&imagingResults=Tumor%20size%20reduced%20to%202.8cm%20(33%%20reduction)"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&laboratoryResults=Mild%20elevation%20in%20liver%20enzymes%20(Grade%201)"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&tumorSizeChange=-33.0"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&adverseEvents=Mild%20rash%20(Grade%201),%20diarrhea%20(Grade%201)"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&performanceStatus=ECOG%201"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&qualityOfLife=Improved%20breathing,%20less%20fatigue"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&clinicalNotes=Patient%20responding%20well%20to%20therapy.%20Side%20effects%20are%20manageable."
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&recordedBy=Dr.%20Smith"

one_month_follow_up_id=$(curl -s -X POST "$BASE_URL$FOLLOW_UP_URL?$FOLLOW_UP_PARAMS" | grep -o '"followUpId":[0-9]*' | grep -o '[0-9]*' | head -1)

echo "1-month follow-up ID: $one_month_follow_up_id"

# 4. Create 3-month follow-up record (complete response)
echo "Creating 3-month follow-up record (complete response)..."

# Set the date to three months from now
three_month_date=$(date -d "3 months" +%Y-%m-%d)

FOLLOW_UP_PARAMS="patientId=$patient_id&therapyId=$therapy_id"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&followUpDate=$three_month_date"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&clinicalStatus=IMPROVED"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&responseAssessment=COMPLETE_RESPONSE"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&imagingResults=No%20visible%20tumor%20on%20imaging"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&laboratoryResults=All%20values%20within%20normal%20range"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&tumorSizeChange=-100.0"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&adverseEvents=Mild%20rash%20persists%20(Grade%201)"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&performanceStatus=ECOG%200"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&qualityOfLife=Returned%20to%20normal%20activities"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&clinicalNotes=Excellent%20response%20to%20therapy.%20Will%20continue%20current%20regimen."
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&recordedBy=Dr.%20Smith"

three_month_follow_up_id=$(curl -s -X POST "$BASE_URL$FOLLOW_UP_URL?$FOLLOW_UP_PARAMS" | grep -o '"followUpId":[0-9]*' | grep -o '[0-9]*' | head -1)

echo "3-month follow-up ID: $three_month_follow_up_id"

# 5. Create 6-month follow-up record (disease progression)
echo "Creating 6-month follow-up record (disease progression)..."

# Set the date to six months from now
six_month_date=$(date -d "6 months" +%Y-%m-%d)

FOLLOW_UP_PARAMS="patientId=$patient_id&therapyId=$therapy_id"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&followUpDate=$six_month_date"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&clinicalStatus=PROGRESSED"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&responseAssessment=PROGRESSIVE_DISEASE"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&imagingResults=New%20lesion%20in%20right%20lung,%20original%20tumor%20site%20shows%201.5cm%20nodule"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&laboratoryResults=Elevated%20inflammatory%20markers"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&tumorSizeChange=100.0"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&adverseEvents=Increased%20cough,%20mild%20dyspnea"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&performanceStatus=ECOG%201"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&qualityOfLife=Some%20reduction%20in%20activity%20tolerance"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&clinicalNotes=Disease%20progression%20after%20initial%20response.%20Suspect%20resistance%20mutation.%20Recommend%20new%20biopsy%20and%20sequencing."
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&requiresNewBiopsy=true"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&requiresNewSequencing=true"
FOLLOW_UP_PARAMS="$FOLLOW_UP_PARAMS&recordedBy=Dr.%20Smith"

six_month_follow_up_id=$(curl -s -X POST "$BASE_URL$FOLLOW_UP_URL?$FOLLOW_UP_PARAMS" | grep -o '"followUpId":[0-9]*' | grep -o '[0-9]*' | head -1)

echo "6-month follow-up ID: $six_month_follow_up_id"

# 6. Get all follow-ups for the patient
echo "Getting all follow-ups for patient $patient_id..."
curl -s "$BASE_URL/patient-follow-ups/patient/$patient_id" | grep -o '"followUpId":[0-9]*' | wc -l

# 7. Get follow-ups requiring new biopsy
echo "Getting follow-ups requiring new biopsy..."
curl -s "$BASE_URL/patient-follow-ups/requiring-biopsy" | grep -o '"followUpId":[0-9]*' | wc -l

# 8. Get follow-ups by therapy
echo "Getting follow-ups for therapy $therapy_id..."
curl -s "$BASE_URL/patient-follow-ups/therapy/$therapy_id" | grep -o '"followUpId":[0-9]*' | wc -l

echo "Test completed!"
echo "You can now access the patient follow-ups at http://localhost:8080/oncology/patient-follow-ups"
