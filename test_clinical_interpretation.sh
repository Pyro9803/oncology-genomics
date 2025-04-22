#!/bin/bash
# Script to test the clinical interpretation and reporting functionality

BASE_URL="http://localhost:8080/oncology"
echo "Testing Clinical Interpretation and Reporting Functionality"
echo "========================================================"

# Function to make API calls
call_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo "API Call: $description"
    
    # Use a temporary file for long data payloads
    if [ -n "$data" ]; then
        echo "$data" > /tmp/curl_data.json
        response=$(curl -s -X $method -H "Content-Type: application/json" -d @/tmp/curl_data.json "$BASE_URL$endpoint")
        rm -f /tmp/curl_data.json
    else
        response=$(curl -s -X $method "$BASE_URL$endpoint")
    fi
    
    echo "Response: $response"
    echo "----------------------------------------"
    
    # Extract ID from response if it's a JSON object with an ID field
    if [[ "$response" == *"id"* ]]; then
        id=$(echo $response | grep -o '"[a-zA-Z]*Id":[0-9]*' | grep -o '[0-9]*' | head -1)
        echo $id
    fi
}

# Wait for the application to start
echo "Waiting for the application to start..."
sleep 5

# 1. Get existing variant callings
echo "Getting existing variant callings..."
variant_callings=$(curl -s "$BASE_URL/variant-callings")
variant_calling_id=$(echo $variant_callings | grep -o '"variantCallingId":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -z "$variant_calling_id" ]; then
    echo "No variant callings found. Please run the laboratory processing workflow first."
    exit 1
fi

echo "Using variant calling ID: $variant_calling_id"

# 2. Get patient and sample IDs
echo "Getting patient and sample IDs..."
patient_id=$(curl -s "$BASE_URL/variant-callings/$variant_calling_id" | grep -o '"patientId":[0-9]*' | grep -o '[0-9]*' | head -1)
sample_id=$(curl -s "$BASE_URL/variant-callings/$variant_calling_id" | grep -o '"sampleId":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -z "$patient_id" ] || [ -z "$sample_id" ]; then
    # Use default values if not found
    patient_id=1
    sample_id=1
    echo "Using default patient ID: $patient_id and sample ID: $sample_id"
else
    echo "Using patient ID: $patient_id and sample ID: $sample_id"
fi

# 3. Get somatic variants for the variant calling
echo "Getting somatic variants..."
somatic_variants=$(curl -s "$BASE_URL/variants/calling/$variant_calling_id")
variant_id=$(echo $somatic_variants | grep -o '"variantId":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -z "$variant_id" ]; then
    echo "No somatic variants found. Please run the variant calling process first."
    exit 1
fi

echo "Using variant ID: $variant_id"

# 4. Create a clinical interpretation for a variant
echo "Creating clinical interpretation..."

# Break down the long URL into manageable parts
INTERPRET_URL="/clinical/variants/$variant_id/interpret"
INTERPRET_PARAMS="pathogenicity=pathogenic&diagnosticSignificance=Diagnostic%20for%20lung%20cancer"
INTERPRET_PARAMS="$INTERPRET_PARAMS&prognosticSignificance=Poor%20prognosis"
INTERPRET_PARAMS="$INTERPRET_PARAMS&therapeuticSignificance=Sensitive%20to%20EGFR%20inhibitors"
INTERPRET_PARAMS="$INTERPRET_PARAMS&evidenceLevel=Level%201A"
INTERPRET_PARAMS="$INTERPRET_PARAMS&evidenceSources=NCCN%20Guidelines"
INTERPRET_PARAMS="$INTERPRET_PARAMS&interpretationSummary=This%20variant%20is%20associated%20with%20response%20to%20EGFR%20inhibitors"
INTERPRET_PARAMS="$INTERPRET_PARAMS&notes=Recommend%20EGFR%20TKI%20therapy"
INTERPRET_PARAMS="$INTERPRET_PARAMS&interpretedBy=Dr.%20Smith"

interpretation_id=$(curl -s -X POST "$BASE_URL$INTERPRET_URL?$INTERPRET_PARAMS" | grep -o '"interpretationId":[0-9]*' | grep -o '[0-9]*' | head -1)

echo "Interpretation ID: $interpretation_id"

# 5. Add a therapy recommendation
echo "Adding therapy recommendation..."

# Break down the long URL into manageable parts
THERAPY_URL="/clinical/interpretations/$interpretation_id/therapy"
THERAPY_PARAMS="therapyName=Gefitinib&therapyType=EGFR%20TKI"
THERAPY_PARAMS="$THERAPY_PARAMS&evidenceLevel=Level%201A"
THERAPY_PARAMS="$THERAPY_PARAMS&evidenceSource=NCCN%20Guidelines"
THERAPY_PARAMS="$THERAPY_PARAMS&referenceId=PMID12345678"
THERAPY_PARAMS="$THERAPY_PARAMS&recommendationSummary=First-line%20therapy%20for%20EGFR-mutant%20NSCLC"
THERAPY_PARAMS="$THERAPY_PARAMS&contraindications=None"

therapy_id=$(curl -s -X POST "$BASE_URL$THERAPY_URL?$THERAPY_PARAMS" | grep -o '"therapyId":[0-9]*' | grep -o '[0-9]*' | head -1)

echo "Therapy ID: $therapy_id"

# 6. Generate a clinical report
echo "Generating clinical report..."

REPORT_URL="/clinical-reports/generate"
REPORT_PARAMS="patientId=$patient_id&sampleId=$sample_id&variantCallingId=$variant_calling_id&createdBy=Dr.%20Smith"

report_id=$(curl -s -X POST "$BASE_URL$REPORT_URL?$REPORT_PARAMS" | grep -o '"reportId":[0-9]*' | grep -o '[0-9]*' | head -1)

echo "Report ID: $report_id"

# 7. Submit the report for review
echo "Submitting report for review..."

SUBMIT_URL="/clinical-reports/$report_id/submit"

submit_response=$(curl -s -X POST "$BASE_URL$SUBMIT_URL")

echo "Submit Response: $submit_response"

# 8. Add a comment to the report
echo "Adding comment to report..."

COMMENT_URL="/clinical-reports/$report_id/comments"
COMMENT_PARAMS="commentText=Please%20review%20the%20therapeutic%20recommendations"
COMMENT_PARAMS="$COMMENT_PARAMS&commentSection=Recommendations"
COMMENT_PARAMS="$COMMENT_PARAMS&commentType=SUGGESTION"
COMMENT_PARAMS="$COMMENT_PARAMS&commentedBy=Dr.%20Johnson"

comment_id=$(curl -s -X POST "$BASE_URL$COMMENT_URL?$COMMENT_PARAMS" | grep -o '"commentId":[0-9]*' | grep -o '[0-9]*' | head -1)

echo "Comment ID: $comment_id"

# 9. Resolve the comment
echo "Resolving comment..."

RESOLVE_URL="/clinical-reports/comments/$comment_id/resolve"
RESOLVE_PARAMS="resolvedBy=Dr.%20Smith&resolutionNotes=Updated%20the%20recommendations"

resolve_response=$(curl -s -X POST "$BASE_URL$RESOLVE_URL?$RESOLVE_PARAMS")

echo "Resolve Response: $resolve_response"

# 10. Approve the report
echo "Approving report..."

APPROVE_URL="/clinical-reports/$report_id/approve"
APPROVE_PARAMS="approvedBy=Dr.%20Johnson"

approve_response=$(curl -s -X POST "$BASE_URL$APPROVE_URL?$APPROVE_PARAMS")

echo "Approve Response: $approve_response"

# 11. Finalize the report
echo "Finalizing report..."

FINALIZE_URL="/clinical-reports/$report_id/finalize"
FINALIZE_PARAMS="pdfPath=/reports/$report_id.pdf"

finalize_response=$(curl -s -X POST "$BASE_URL$FINALIZE_URL?$FINALIZE_PARAMS")

echo "Finalize Response: $finalize_response"

# 12. Get the report
echo "Getting report..."

GET_REPORT_URL="/clinical-reports/$report_id"

report_response=$(curl -s -X GET "$BASE_URL$GET_REPORT_URL")

echo "Report Response: $report_response"

echo "Test completed!"
echo "You can now access the clinical reports at http://localhost:8080/oncology/clinical-reports"
