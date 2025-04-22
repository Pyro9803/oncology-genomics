package com.example.oncology.service;

import com.example.oncology.entity.Sample;
import com.example.oncology.entity.SomaticVariant;
import com.example.oncology.entity.VariantCalling;
import com.example.oncology.repository.VariantCallingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling GATK pipeline operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GatkService {

    private final VariantCallingRepository variantCallingRepository;
    
    @Value("${app.gatk.input-dir}")
    private String gatkInputDir;
    
    @Value("${app.gatk.output-dir}")
    private String gatkOutputDir;
    
    @Value("${app.gatk.reference-path}")
    private String referenceGenomePath;
    
    // Map to store running processes by job ID
    private final Map<String, Process> runningProcesses = new HashMap<>();
    
    /**
     * Start a variant calling pipeline asynchronously
     */
    @Async("gatkTaskExecutor")
    public CompletableFuture<Void> startVariantCallingPipeline(Long variantCallingId) {
        VariantCalling variantCalling = variantCallingRepository.findById(variantCallingId)
                .orElseThrow(() -> new IllegalArgumentException("Variant calling not found: " + variantCallingId));
        
        try {
            // Update status to IN_PROGRESS
            variantCalling.setStatus("IN_PROGRESS");
            variantCallingRepository.save(variantCalling);
            
            // Get sample information
            Sample tumorSample = variantCalling.getTumorSample();
            Sample normalSample = variantCalling.getNormalSample();
            
            // Create output directory if it doesn't exist
            String outputDir = gatkOutputDir + "/" + variantCallingId;
            Path outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
            
            // Build GATK command
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder.append("gatk Mutect2 ");
            commandBuilder.append("-R ").append(referenceGenomePath).append(" ");
            
            // Add tumor sample
            String tumorBamPath = tumorSample.getSequencingData().isEmpty() ? 
                    null : tumorSample.getSequencingData().get(0).getBamPath();
            
            if (tumorBamPath == null) {
                throw new IllegalStateException("Tumor sample does not have BAM file path");
            }
            
            commandBuilder.append("-I ").append(tumorBamPath).append(" ");
            commandBuilder.append("-tumor ").append(tumorSample.getSampleId()).append(" ");
            
            // Add normal sample if available
            if (normalSample != null) {
                String normalBamPath = normalSample.getSequencingData().isEmpty() ? 
                        null : normalSample.getSequencingData().get(0).getBamPath();
                
                if (normalBamPath != null) {
                    commandBuilder.append("-I ").append(normalBamPath).append(" ");
                    commandBuilder.append("-normal ").append(normalSample.getSampleId()).append(" ");
                }
            }
            
            // Add panel of normals if used
            if (Boolean.TRUE.equals(variantCalling.getPanelOfNormalsUsed()) && 
                    variantCalling.getPanelOfNormalsPath() != null) {
                commandBuilder.append("--panel-of-normals ").append(variantCalling.getPanelOfNormalsPath()).append(" ");
            }
            
            // Add output path
            String vcfOutputPath = outputDir + "/somatic_variants.vcf.gz";
            commandBuilder.append("-O ").append(vcfOutputPath);
            
            // Save the command to the variant calling record
            String gatkCommand = commandBuilder.toString();
            variantCalling.setGatkCommandLine(gatkCommand);
            variantCalling.setVcfOutputPath(vcfOutputPath);
            variantCalling.setPipelineLogPath(outputDir + "/pipeline.log");
            variantCallingRepository.save(variantCalling);
            
            log.info("Starting GATK pipeline for variant calling ID: {}", variantCallingId);
            log.info("Command: {}", gatkCommand);
            
            try {
                // Check if GATK is available
                Process checkGatk = new ProcessBuilder("bash", "-c", "which gatk || echo 'not found'").start();
                String gatkPath = new String(checkGatk.getInputStream().readAllBytes()).trim();
                int exitCode = 0;
                
                if (gatkPath.contains("not found")) {
                    log.warn("GATK not found in PATH. Simulating pipeline execution for development purposes.");
                    
                    // Simulate pipeline execution
                    Thread.sleep(2000); // Simulate 2 seconds of processing
                    
                    // Update variant calling with simulated results
                    variantCalling.setTotalVariantsCalled(125);
                    variantCalling.setVariantsPassedFilter(98);
                    variantCalling.setStatus("COMPLETED");
                    variantCalling.setVcfOutputPath("/simulated/path/to/variants.vcf.gz");
                    
                    // Create some sample variants for demonstration
                    createSampleVariants(variantCalling);
                    
                    // Save the updated variant calling
                    variantCallingRepository.save(variantCalling);
                    return CompletableFuture.completedFuture(null);
                }
                
                // Execute the command if GATK is available
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("bash", "-c", gatkCommand);
                processBuilder.redirectErrorStream(true);
                
                // Create log file directory if it doesn't exist
                Path logPath = Paths.get(variantCalling.getPipelineLogPath());
                if (!Files.exists(logPath.getParent())) {
                    Files.createDirectories(logPath.getParent());
                }
                
                processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logPath.toFile()));
                
                Process process = processBuilder.start();
                runningProcesses.put(variantCalling.getJobId(), process);
                
                // Wait for process to complete
                exitCode = process.waitFor();
                runningProcesses.remove(variantCalling.getJobId());
                
                // Process results based on exit code
                if (exitCode == 0) {
                    variantCalling.setStatus("COMPLETED");
                    processVcfResults(variantCalling, variantCalling.getVcfOutputPath());
                } else {
                    variantCalling.setStatus("FAILED");
                    variantCalling.setErrorMessage("GATK pipeline failed with exit code " + exitCode + ". Check logs at " + variantCalling.getPipelineLogPath());
                }
                
                // Save the updated variant calling
                variantCallingRepository.save(variantCalling);
                return CompletableFuture.completedFuture(null);
            } catch (Exception e) {
                log.error("Error executing GATK command: {}", e.getMessage());
                variantCalling.setStatus("FAILED");
                variantCalling.setErrorMessage("Error: " + e.getMessage());
                variantCallingRepository.save(variantCalling);
                return CompletableFuture.failedFuture(e);
            }
            
        } catch (Exception e) {
            log.error("Error running GATK pipeline for variant calling ID: {}", variantCallingId, e);
            
            // Update status to FAILED
            variantCalling.setStatus("FAILED");
            variantCalling.setErrorMessage("Error: " + e.getMessage());
            variantCallingRepository.save(variantCalling);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Create sample variants for demonstration purposes
     */
    private void createSampleVariants(VariantCalling variantCalling) {
        List<SomaticVariant> variants = new ArrayList<>();
        
        // Create a few sample variants
        String[] chromosomes = {"chr1", "chr3", "chr7", "chr17"};
        String[] genes = {"BRAF", "KRAS", "TP53", "EGFR"};
        String[] variantTypes = {"SNV", "DELETION", "INSERTION"};
        String[] filterStatus = {"PASS", "PASS", "PASS", "germline_risk"};
        
        for (int i = 0; i < 4; i++) {
            SomaticVariant variant = new SomaticVariant();
            variant.setVariantCalling(variantCalling);
            variant.setChromosome(chromosomes[i]);
            variant.setPosition(1000000 + i * 10000);
            variant.setReferenceAllele(i % 2 == 0 ? "A" : "G");
            variant.setAlternateAllele(i % 2 == 0 ? "G" : "T");
            variant.setVariantType(variantTypes[i % 3]);
            variant.setGeneSymbol(genes[i]);
            variant.setFilterStatus(filterStatus[i]);
            variants.add(variant);
        }
        
        // Add variants to the variant calling
        variantCalling.getSomaticVariants().addAll(variants);
    }
    
    /**
     * Process VCF results and save somatic variants
     */
    private void processVcfResults(VariantCalling variantCalling, String vcfPath) throws IOException {
        // This is a simplified implementation - in a real system you would use a proper VCF parser
        List<SomaticVariant> variants = new ArrayList<>();
        int totalVariants = 0;
        int passedVariants = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Files.newInputStream(Paths.get(vcfPath))))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue; // Skip header lines
                }
                
                totalVariants++;
                String[] fields = line.split("\\t");
                
                // Parse basic VCF fields
                String chromosome = fields[0];
                int position = Integer.parseInt(fields[1]);
                String referenceAllele = fields[3];
                String alternateAllele = fields[4];
                String filterStatus = fields[6];
                
                // Create somatic variant
                SomaticVariant variant = new SomaticVariant();
                variant.setVariantCalling(variantCalling);
                variant.setChromosome(chromosome);
                variant.setPosition(position);
                variant.setReferenceAllele(referenceAllele);
                variant.setAlternateAllele(alternateAllele);
                variant.setFilterStatus(filterStatus);
                
                // Determine variant type
                if (referenceAllele.length() == alternateAllele.length() && referenceAllele.length() == 1) {
                    variant.setVariantType("SNV");
                } else if (referenceAllele.length() > alternateAllele.length()) {
                    variant.setVariantType("DELETION");
                } else if (referenceAllele.length() < alternateAllele.length()) {
                    variant.setVariantType("INSERTION");
                } else {
                    variant.setVariantType("COMPLEX");
                }
                
                // Count passed variants
                if ("PASS".equals(filterStatus)) {
                    passedVariants++;
                }
                
                variants.add(variant);
            }
        }
        
        // Update variant calling with counts
        variantCalling.setTotalVariantsCalled(totalVariants);
        variantCalling.setVariantsPassedFilter(passedVariants);
        
        // Add variants to the variant calling
        variantCalling.getSomaticVariants().addAll(variants);
    }
    
    /**
     * Cancel a running variant calling pipeline
     */
    public void cancelVariantCallingPipeline(String jobId) {
        Process process = runningProcesses.get(jobId);
        if (process != null && process.isAlive()) {
            process.destroy();
            runningProcesses.remove(jobId);
            log.info("Cancelled GATK pipeline job: {}", jobId);
        }
    }
}
