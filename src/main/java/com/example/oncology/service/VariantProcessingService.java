package com.example.oncology.service;

import com.example.oncology.entity.*;
import com.example.oncology.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for processing VCF files and storing variants in the database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VariantProcessingService {

    private final VariantCallingRepository variantCallingRepository;
    private final SomaticVariantRepository somaticVariantRepository;
    private final AnnotationRepository annotationRepository;
    
    @Value("${app.gatk.output-dir:/data/outputs}")
    private String outputDir;
    
    /**
     * Process a completed GATK analysis job
     * @param jobId ID of the completed job
     */
    @Async
    @Transactional
    public void processCompletedJob(Long jobId) {
        VariantCalling variantCalling = variantCallingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        if (!"COMPLETED".equals(variantCalling.getStatus())) {
            throw new IllegalStateException("Cannot process job that is not completed: " + jobId);
        }
        
        try {
            // Get the VCF file path
            String vcfFilePath = variantCalling.getVcfOutputPath();
            if (vcfFilePath == null) {
                throw new IllegalStateException("VCF file path not found for job: " + jobId);
            }
            
            Path fullVcfPath = Paths.get(outputDir, vcfFilePath);
            
            // Parse VCF file and store variants
            List<SomaticVariant> variants = parseVcfFile(fullVcfPath.toString(), variantCalling);
            
            // Save variants to database
            somaticVariantRepository.saveAll(variants);
            
            // Update job status
            variantCalling.setProgressLog("Processed " + variants.size() + " variants");
            variantCallingRepository.save(variantCalling);
            
            log.info("Processed {} variants for job {}", variants.size(), jobId);
        } catch (Exception e) {
            log.error("Failed to process variants for job " + jobId, e);
            variantCalling.setErrorMessage("Failed to process variants: " + e.getMessage());
            variantCallingRepository.save(variantCalling);
        }
    }
    
    /**
     * Parse a VCF file and create SomaticVariant entities
     */
    private List<SomaticVariant> parseVcfFile(String vcfFilePath, VariantCalling variantCalling) throws IOException {
        List<SomaticVariant> variants = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(vcfFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip header lines
                if (line.startsWith("#")) {
                    continue;
                }
                
                // Parse variant line
                String[] fields = line.split("\\t");
                if (fields.length < 8) {
                    continue; // Invalid line
                }
                
                String chromosome = fields[0];
                int position = Integer.parseInt(fields[1]);
                String referenceAllele = fields[3];
                String alternateAllele = fields[4];
                String filterStatus = fields[6];
                
                // Parse INFO field to extract additional information
                String info = fields[7];
                
                // Extract variant type (SNV, insertion, deletion)
                String variantType = determineVariantType(referenceAllele, alternateAllele);
                
                // Extract allele frequency (AF) from INFO field
                BigDecimal alleleFrequency = extractAlleleFrequency(info);
                
                // Extract read depth (DP) from INFO field
                Integer readDepth = extractReadDepth(info);
                
                // Extract alt read count from INFO field
                Integer altReadCount = extractAltReadCount(info);
                
                // Extract gene symbol from INFO field
                String geneSymbol = extractGeneSymbol(info);
                
                // Create SomaticVariant entity
                SomaticVariant variant = new SomaticVariant();
                variant.setVariantCalling(variantCalling);
                variant.setChromosome(chromosome);
                variant.setPosition(position);
                variant.setReferenceAllele(referenceAllele);
                variant.setAlternateAllele(alternateAllele);
                variant.setVariantType(variantType);
                variant.setGeneSymbol(geneSymbol);
                variant.setAlleleFrequency(alleleFrequency);
                variant.setReadDepth(readDepth);
                variant.setAltReadCount(altReadCount);
                variant.setFilterStatus(filterStatus);
                
                // Create annotation entity
                Annotation annotation = createAnnotation(variant, info);
                variant.setAnnotation(annotation);
                
                variants.add(variant);
            }
        }
        
        return variants;
    }
    
    /**
     * Determine the variant type based on reference and alternate alleles
     */
    private String determineVariantType(String referenceAllele, String alternateAllele) {
        if (referenceAllele.length() == 1 && alternateAllele.length() == 1) {
            return "SNV";
        } else if (referenceAllele.length() > alternateAllele.length()) {
            return "DELETION";
        } else if (referenceAllele.length() < alternateAllele.length()) {
            return "INSERTION";
        } else {
            return "COMPLEX";
        }
    }
    
    /**
     * Extract allele frequency from INFO field
     */
    private BigDecimal extractAlleleFrequency(String info) {
        Pattern pattern = Pattern.compile("AF=([0-9.]+)");
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        return null;
    }
    
    /**
     * Extract read depth from INFO field
     */
    private Integer extractReadDepth(String info) {
        Pattern pattern = Pattern.compile("DP=([0-9]+)");
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
    
    /**
     * Extract alternate read count from INFO field
     */
    private Integer extractAltReadCount(String info) {
        Pattern pattern = Pattern.compile("AD=([0-9]+),([0-9]+)");
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(2));
        }
        return null;
    }
    
    /**
     * Extract gene symbol from INFO field
     */
    private String extractGeneSymbol(String info) {
        Pattern pattern = Pattern.compile("Gene=([^;]+)");
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Create an annotation entity from INFO field
     */
    private Annotation createAnnotation(SomaticVariant variant, String info) {
        Annotation annotation = new Annotation();
        annotation.setSomaticVariant(variant);
        
        // Save the annotation using the repository
        annotation = annotationRepository.save(annotation);
        
        // Extract gene
        String gene = extractGeneSymbol(info);
        annotation.setGene(gene != null ? gene : "Unknown");
        
        // Extract consequence
        Pattern consequencePattern = Pattern.compile("Consequence=([^;]+)");
        Matcher consequenceMatcher = consequencePattern.matcher(info);
        if (consequenceMatcher.find()) {
            annotation.setConsequence(consequenceMatcher.group(1));
        }
        
        // Extract impact
        Pattern impactPattern = Pattern.compile("IMPACT=([^;]+)");
        Matcher impactMatcher = impactPattern.matcher(info);
        if (impactMatcher.find()) {
            annotation.setImpact(impactMatcher.group(1));
        }
        
        // Extract dbSNP ID
        Pattern dbsnpPattern = Pattern.compile("DB=([^;]+)");
        Matcher dbsnpMatcher = dbsnpPattern.matcher(info);
        if (dbsnpMatcher.find()) {
            annotation.setDbsnpId(dbsnpMatcher.group(1));
        }
        
        // Extract COSMIC ID
        Pattern cosmicPattern = Pattern.compile("COSMIC=([^;]+)");
        Matcher cosmicMatcher = cosmicPattern.matcher(info);
        if (cosmicMatcher.find()) {
            annotation.setCosmicId(cosmicMatcher.group(1));
        }
        
        return annotation;
    }
    
    /**
     * Annotate variants with external data sources
     */
    @Async
    @Transactional
    public void annotateVariants(Long jobId) {
        // In a real application, this method would call external APIs like VEP, OncoKB, etc.
        // to annotate variants with additional information
        log.info("Annotating variants for job {}", jobId);
        
        // For demonstration purposes, we'll just log a message
        VariantCalling variantCalling = variantCallingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        variantCalling.setProgressLog("Variants annotated with external data sources");
        variantCallingRepository.save(variantCalling);
    }
}
