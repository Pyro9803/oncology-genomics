package com.example.oncology.service;

import com.example.oncology.entity.Sample;
import com.example.oncology.entity.VariantCalling;
import com.example.oncology.repository.SampleRepository;
import com.example.oncology.repository.VariantCallingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatkAnalysisService {

    private final SampleRepository sampleRepository;
    private final VariantCallingRepository variantCallingRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.gatk.input-dir:/data/inputs}")
    private String inputDir;
    
    @Value("${app.gatk.output-dir:/data/outputs}")
    private String outputDir;
    
    @Value("${app.gatk.reference-path:/data/references/GRCh38.fa}")
    private String referencePath;

    /**
     * Submit a new GATK analysis job
     * @param tumorSampleId ID of the tumor sample
     * @param normalSampleId ID of the normal sample (can be null for tumor-only analysis)
     * @param pipelineVersion GATK pipeline version
     * @param callingMethod Variant calling method (e.g., Mutect2)
     * @return The created VariantCalling entity with job details
     */
    public VariantCalling submitAnalysisJob(Long tumorSampleId, Long normalSampleId, 
                                           String pipelineVersion, String callingMethod) {
        
        // Validate tumor sample exists
        Sample tumorSample = sampleRepository.findById(tumorSampleId)
                .orElseThrow(() -> new IllegalArgumentException("Tumor sample not found: " + tumorSampleId));
        
        // Get normal sample if provided
        Sample normalSample = null;
        if (normalSampleId != null) {
            normalSample = sampleRepository.findById(normalSampleId)
                    .orElseThrow(() -> new IllegalArgumentException("Normal sample not found: " + normalSampleId));
        }
        
        // Create new variant calling job
        VariantCalling variantCalling = new VariantCalling();
        variantCalling.setTumorSample(tumorSample);
        variantCalling.setNormalSample(normalSample);
        variantCalling.setPipelineVersion(pipelineVersion);
        variantCalling.setReferenceGenome("GRCh38"); // Default reference genome
        variantCalling.setCallingMethod(callingMethod);
        variantCalling.setStatus("PENDING");
        variantCalling.setStartTime(LocalDateTime.now());
        
        // Save to get an ID
        variantCalling = variantCallingRepository.save(variantCalling);
        
        // Submit job to GATK runner
        submitJobToGatk(variantCalling);
        
        return variantCalling;
    }
    
    /**
     * Create a job request file for the GATK runner
     */
    @Async
    protected void submitJobToGatk(VariantCalling variantCalling) {
        try {
            // Create directories if they don't exist
            Path inputDirPath = Paths.get(inputDir);
            if (!Files.exists(inputDirPath)) {
                Files.createDirectories(inputDirPath);
            }
            
            // Create JSON job request
            ObjectNode jobRequest = objectMapper.createObjectNode();
            jobRequest.put("job_id", variantCalling.getVariantCallingId().toString());
            
            // Get BAM paths from sequencing data
            // In a real application, you would get the actual BAM path from the sequencing data
            String tumorBamPath = "sample_" + variantCalling.getTumorSample().getSampleId() + ".bam";
            jobRequest.put("tumor_bam", tumorBamPath);
            
            if (variantCalling.getNormalSample() != null) {
                String normalBamPath = "sample_" + variantCalling.getNormalSample().getSampleId() + ".bam";
                jobRequest.put("normal_bam", normalBamPath);
            }
            
            // Additional parameters
            jobRequest.put("reference_genome", referencePath);
            jobRequest.put("calling_method", variantCalling.getCallingMethod());
            
            // Write job request to file
            String jobFileName = "job_request_" + variantCalling.getVariantCallingId() + ".json";
            File jobFile = new File(inputDir, jobFileName);
            objectMapper.writeValue(jobFile, jobRequest);
            
            // Update job status
            variantCalling.setStatus("SUBMITTED");
            variantCalling.setProgressLog("Job submitted to GATK runner");
            variantCallingRepository.save(variantCalling);
            
            log.info("GATK analysis job submitted: {}", variantCalling.getVariantCallingId());
        } catch (IOException e) {
            log.error("Failed to submit GATK job", e);
            variantCalling.setStatus("FAILED");
            variantCalling.setErrorMessage("Failed to submit job: " + e.getMessage());
            variantCallingRepository.save(variantCalling);
        }
    }
    
    /**
     * Check the status of a GATK analysis job
     */
    public VariantCalling checkJobStatus(Long jobId) {
        VariantCalling variantCalling = variantCallingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        // If job is already completed or failed, just return current status
        if ("COMPLETED".equals(variantCalling.getStatus()) || "FAILED".equals(variantCalling.getStatus())) {
            return variantCalling;
        }
        
        // Check status file from GATK runner
        try {
            String statusFileName = jobId + ".status";
            Path statusFilePath = Paths.get(outputDir, statusFileName);
            
            if (Files.exists(statusFilePath)) {
                String statusContent = Files.readString(statusFilePath);
                ObjectNode statusJson = (ObjectNode) objectMapper.readTree(statusContent);
                
                String status = statusJson.get("status").asText();
                if ("completed".equals(status)) {
                    variantCalling.setStatus("COMPLETED");
                    variantCalling.setEndTime(LocalDateTime.now());
                    variantCalling.setVcfOutputPath(statusJson.get("vcf_path").asText());
                    
                    // In a real application, you would parse the VCF file and store variants in the database
                    // This would be handled by another service
                    
                } else if ("failed".equals(status)) {
                    variantCalling.setStatus("FAILED");
                    variantCalling.setEndTime(LocalDateTime.now());
                    variantCalling.setErrorMessage(statusJson.has("error") ? 
                            statusJson.get("error").asText() : "Unknown error");
                } else {
                    variantCalling.setStatus("RUNNING");
                    if (statusJson.has("progress")) {
                        variantCalling.setProgressLog(statusJson.get("progress").asText());
                    }
                }
                
                variantCallingRepository.save(variantCalling);
            }
        } catch (IOException e) {
            log.error("Failed to check job status", e);
        }
        
        return variantCalling;
    }
    
    /**
     * Cancel a running GATK analysis job
     */
    public boolean cancelJob(Long jobId) {
        VariantCalling variantCalling = variantCallingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        if ("COMPLETED".equals(variantCalling.getStatus()) || "FAILED".equals(variantCalling.getStatus())) {
            return false; // Cannot cancel completed or failed jobs
        }
        
        try {
            // Create a cancel request file
            String cancelFileName = "cancel_" + jobId + ".json";
            File cancelFile = new File(inputDir, cancelFileName);
            ObjectNode cancelRequest = objectMapper.createObjectNode();
            cancelRequest.put("job_id", jobId.toString());
            cancelRequest.put("action", "cancel");
            objectMapper.writeValue(cancelFile, cancelRequest);
            
            // Update job status
            variantCalling.setStatus("CANCELLING");
            variantCallingRepository.save(variantCalling);
            
            return true;
        } catch (IOException e) {
            log.error("Failed to cancel job", e);
            return false;
        }
    }
}
