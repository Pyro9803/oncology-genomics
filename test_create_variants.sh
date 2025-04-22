#!/bin/bash
# Script to create test somatic variants for clinical interpretation testing

BASE_URL="http://localhost:8080/oncology"
echo "Creating Test Somatic Variants"
echo "============================="

# Function to make API calls
call_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo "API Call: $description"
    
    if [ -n "$data" ]; then
        response=$(curl -s -X $method -H "Content-Type: application/json" -H "Accept: application/json" -d "$data" "$BASE_URL$endpoint")
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

# 2. Create test variants
echo "Creating test variants..."

# EGFR L858R mutation - common in lung cancer, sensitive to EGFR inhibitors
egfr_variant_data='{
    "chromosome": "7",
    "position": 55259515,
    "referenceAllele": "T",
    "alternateAllele": "G",
    "geneSymbol": "EGFR",
    "variantType": "SNV",
    "variantEffect": "missense_variant",
    "transcriptId": "NM_005228.5",
    "hgvsCoding": "c.2573T>G",
    "hgvsProtein": "p.Leu858Arg",
    "readDepth": 500,
    "alleleFrequency": 0.35,
    "cosmicId": "COSM6224",
    "dbsnpId": "rs121434568"
}'

egfr_variant_id=$(call_api "POST" "/variants/variant-calling/$variant_calling_id" "$egfr_variant_data" "Create EGFR L858R Variant")

# BRAF V600E mutation - common in melanoma, sensitive to BRAF inhibitors
braf_variant_data='{
    "chromosome": "7",
    "position": 140453136,
    "referenceAllele": "T",
    "alternateAllele": "A",
    "geneSymbol": "BRAF",
    "variantType": "SNV",
    "variantEffect": "missense_variant",
    "transcriptId": "NM_004333.4",
    "hgvsCoding": "c.1799T>A",
    "hgvsProtein": "p.Val600Glu",
    "readDepth": 450,
    "alleleFrequency": 0.42,
    "cosmicId": "COSM476",
    "dbsnpId": "rs113488022"
}'

braf_variant_id=$(call_api "POST" "/variants/variant-calling/$variant_calling_id" "$braf_variant_data" "Create BRAF V600E Variant")

# TP53 R175H mutation - common in many cancers, associated with poor prognosis
tp53_variant_data='{
    "chromosome": "17",
    "position": 7578406,
    "referenceAllele": "G",
    "alternateAllele": "A",
    "geneSymbol": "TP53",
    "variantType": "SNV",
    "variantEffect": "missense_variant",
    "transcriptId": "NM_000546.5",
    "hgvsCoding": "c.524G>A",
    "hgvsProtein": "p.Arg175His",
    "readDepth": 380,
    "alleleFrequency": 0.28,
    "cosmicId": "COSM10648",
    "dbsnpId": "rs28934578"
}'

tp53_variant_id=$(call_api "POST" "/variants/variant-calling/$variant_calling_id" "$tp53_variant_data" "Create TP53 R175H Variant")

echo "Created variants successfully!"
echo "EGFR L858R Variant ID: $egfr_variant_id"
echo "BRAF V600E Variant ID: $braf_variant_id"
echo "TP53 R175H Variant ID: $tp53_variant_id"
