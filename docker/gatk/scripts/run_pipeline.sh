#!/bin/bash
set -e

# Default parameters (can be overridden by environment variables)
REFERENCE=${REFERENCE_PATH:-"/data/references/GRCh38.fa"}
DBSNP=${DBSNP_PATH:-"/data/references/dbsnp.vcf.gz"}
INPUT_DIR=${INPUT_DIR:-"/data/inputs"}
OUTPUT_DIR=${OUTPUT_DIR:-"/data/outputs"}

# Function to run Mutect2 somatic variant calling
run_mutect2() {
    local tumor_bam=$1
    local normal_bam=$2
    local output_vcf=$3
    local tumor_name=$(basename "$tumor_bam" .bam)
    local normal_name=$(basename "$normal_bam" .bam)
    
    echo "Running Mutect2 on ${tumor_name} vs ${normal_name}..."
    
    gatk Mutect2 \
        -R "$REFERENCE" \
        -I "$tumor_bam" \
        -I "$normal_bam" \
        -tumor "$tumor_name" \
        -normal "$normal_name" \
        --germline-resource "$DBSNP" \
        -O "$output_vcf"
        
    echo "Mutect2 completed for ${tumor_name} vs ${normal_name}"
}

# Function to filter variants
filter_variants() {
    local input_vcf=$1
    local filtered_vcf=$2
    
    echo "Filtering variants in ${input_vcf}..."
    
    gatk FilterMutectCalls \
        -R "$REFERENCE" \
        -V "$input_vcf" \
        -O "$filtered_vcf"
        
    echo "Variant filtering completed for ${input_vcf}"
}

# Function to annotate variants
annotate_variants() {
    local input_vcf=$1
    local annotated_vcf=$2
    
    echo "Annotating variants in ${input_vcf}..."
    
    gatk Funcotator \
        --variant "$input_vcf" \
        --reference "$REFERENCE" \
        --ref-version hg38 \
        --data-sources-path /data/references/funcotator_dataSources.v1.7.20200521g \
        --output "$annotated_vcf" \
        --output-file-format VCF
        
    echo "Variant annotation completed for ${input_vcf}"
}

# Main function to process a tumor-normal pair
process_pair() {
    local tumor_bam=$1
    local normal_bam=$2
    local pair_id=$3
    
    local output_dir="${OUTPUT_DIR}/${pair_id}"
    mkdir -p "$output_dir"
    
    local raw_vcf="${output_dir}/${pair_id}.raw.vcf"
    local filtered_vcf="${output_dir}/${pair_id}.filtered.vcf"
    local annotated_vcf="${output_dir}/${pair_id}.annotated.vcf"
    
    # Run the somatic variant calling pipeline
    run_mutect2 "$tumor_bam" "$normal_bam" "$raw_vcf"
    filter_variants "$raw_vcf" "$filtered_vcf"
    annotate_variants "$filtered_vcf" "$annotated_vcf"
    
    echo "Processing completed for pair ${pair_id}"
    
    # Create a status file to indicate completion
    echo "COMPLETED" > "${output_dir}/status.txt"
}

# Check for job request files
check_for_jobs() {
    for job_file in "${INPUT_DIR}"/*.job; do
        if [ -f "$job_file" ]; then
            echo "Found job file: $job_file"
            
            # Parse job file (expected format: JSON with tumor_bam, normal_bam, and pair_id fields)
            tumor_bam=$(jq -r '.tumor_bam' "$job_file")
            normal_bam=$(jq -r '.normal_bam' "$job_file")
            pair_id=$(jq -r '.pair_id' "$job_file")
            
            if [ -f "$tumor_bam" ] && [ -f "$normal_bam" ]; then
                echo "Processing pair $pair_id: $tumor_bam vs $normal_bam"
                
                # Move job file to in-progress status
                mv "$job_file" "${job_file}.in_progress"
                
                # Process the tumor-normal pair
                process_pair "$tumor_bam" "$normal_bam" "$pair_id"
                
                # Move job file to completed status
                mv "${job_file}.in_progress" "${job_file}.completed"
            else
                echo "Error: BAM files not found for job $job_file"
                mv "$job_file" "${job_file}.error"
            fi
        fi
    done
}

# Main loop
echo "GATK pipeline service started"
echo "Monitoring for job requests in $INPUT_DIR"

while true; do
    check_for_jobs
    sleep 60
done
