#!/bin/bash
# Script to test the variant annotation functionality

BASE_URL="http://localhost:8080/oncology"
echo "Testing Variant Annotation Functionality"
echo "========================================"

# Function to make API calls
call_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo "API Call: $description"
    
    if [ -n "$data" ]; then
        response=$(curl -s -X $method -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
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

# 2. Start the annotation process for the variant calling
echo "Starting variant annotation process..."
call_api "POST" "/variant-annotations/annotate/$variant_calling_id" "" "Start Annotation"

# 3. Wait for annotation to complete (in a real scenario, this would be asynchronous)
echo "Waiting for annotation to complete..."
sleep 5

# 4. Get annotations for the variant calling
echo "Getting annotations for variant calling..."
call_api "GET" "/variant-annotations/variant-calling/$variant_calling_id" "" "Get Annotations"

# 5. Get pathogenic variants
echo "Getting pathogenic variants..."
call_api "GET" "/variant-annotations/pathogenic" "" "Get Pathogenic Variants"

# 6. Get clinically relevant variants
echo "Getting clinically relevant variants..."
call_api "GET" "/variant-annotations/clinically-relevant" "" "Get Clinically Relevant Variants"

# 7. Get actionable variants
echo "Getting actionable variants..."
call_api "GET" "/variant-annotations/actionable" "" "Get Actionable Variants"

# 8. Filter variants by criteria
echo "Filtering variants..."
call_api "GET" "/variant-annotations/filter/$variant_calling_id?variantEffect=missense_variant&maxPopulationFrequency=0.01" "" "Filter Variants"

echo "Test completed!"
echo "You can now access the variant annotations at http://localhost:8080/oncology/api/variant-annotations"
