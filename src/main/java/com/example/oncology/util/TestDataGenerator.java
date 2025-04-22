package com.example.oncology.util;

import com.example.oncology.entity.SomaticVariant;
import com.example.oncology.entity.VariantCalling;
import com.example.oncology.repository.SomaticVariantRepository;
import com.example.oncology.repository.VariantCallingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Utility class to generate test data for development and testing
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class TestDataGenerator {

    private final VariantCallingRepository variantCallingRepository;
    private final SomaticVariantRepository somaticVariantRepository;

    @Bean
    public CommandLineRunner generateTestVariants() {
        return args -> {
            log.info("Checking for existing variant callings...");
            Optional<VariantCalling> variantCallingOpt = variantCallingRepository.findById(1L);
            
            if (variantCallingOpt.isPresent()) {
                VariantCalling variantCalling = variantCallingOpt.get();
                
                // Check if we already have variants for this calling
                if (!somaticVariantRepository.findByVariantCalling(variantCalling).isEmpty()) {
                    log.info("Test variants already exist, skipping generation");
                    return;
                }
                
                log.info("Generating test variants for variant calling ID: {}", variantCalling.getVariantCallingId());
                
                // EGFR L858R mutation - common in lung cancer, sensitive to EGFR inhibitors
                SomaticVariant egfrVariant = new SomaticVariant();
                egfrVariant.setVariantCalling(variantCalling);
                egfrVariant.setChromosome("7");
                egfrVariant.setPosition(55259515);
                egfrVariant.setReferenceAllele("T");
                egfrVariant.setAlternateAllele("G");
                egfrVariant.setGeneSymbol("EGFR");
                egfrVariant.setVariantType("SNV");
                // Set filter status (required field)
                egfrVariant.setFilterStatus("PASS");
                egfrVariant.setTranscriptId("NM_005228.5");
                egfrVariant.setHgvsC("c.2573T>G");
                egfrVariant.setHgvsP("p.Leu858Arg");
                egfrVariant.setReadDepth(500);
                egfrVariant.setAlleleFrequency(new BigDecimal("0.35"));
                // Calculate alt read count based on depth and frequency
                egfrVariant.setAltReadCount((int)(500 * 0.35));
                // No direct dbsnpId field, could be stored in annotation
                somaticVariantRepository.save(egfrVariant);
                log.info("Created EGFR L858R variant with ID: {}", egfrVariant.getVariantId());
                
                // BRAF V600E mutation - common in melanoma, sensitive to BRAF inhibitors
                SomaticVariant brafVariant = new SomaticVariant();
                brafVariant.setVariantCalling(variantCalling);
                brafVariant.setChromosome("7");
                brafVariant.setPosition(140453136);
                brafVariant.setReferenceAllele("T");
                brafVariant.setAlternateAllele("A");
                brafVariant.setGeneSymbol("BRAF");
                brafVariant.setVariantType("SNV");
                // Set filter status (required field)
                brafVariant.setFilterStatus("PASS");
                brafVariant.setTranscriptId("NM_004333.4");
                brafVariant.setHgvsC("c.1799T>A");
                brafVariant.setHgvsP("p.Val600Glu");
                brafVariant.setReadDepth(450);
                brafVariant.setAlleleFrequency(new BigDecimal("0.42"));
                // Calculate alt read count based on depth and frequency
                brafVariant.setAltReadCount((int)(450 * 0.42));
                // No direct dbsnpId field, could be stored in annotation
                somaticVariantRepository.save(brafVariant);
                log.info("Created BRAF V600E variant with ID: {}", brafVariant.getVariantId());
                
                // TP53 R175H mutation - common in many cancers, associated with poor prognosis
                SomaticVariant tp53Variant = new SomaticVariant();
                tp53Variant.setVariantCalling(variantCalling);
                tp53Variant.setChromosome("17");
                tp53Variant.setPosition(7578406);
                tp53Variant.setReferenceAllele("G");
                tp53Variant.setAlternateAllele("A");
                tp53Variant.setGeneSymbol("TP53");
                tp53Variant.setVariantType("SNV");
                // Set filter status (required field)
                tp53Variant.setFilterStatus("PASS");
                tp53Variant.setTranscriptId("NM_000546.5");
                tp53Variant.setHgvsC("c.524G>A");
                tp53Variant.setHgvsP("p.Arg175His");
                tp53Variant.setReadDepth(380);
                tp53Variant.setAlleleFrequency(new BigDecimal("0.28"));
                // Calculate alt read count based on depth and frequency
                tp53Variant.setAltReadCount((int)(380 * 0.28));
                // No direct dbsnpId field, could be stored in annotation
                somaticVariantRepository.save(tp53Variant);
                log.info("Created TP53 R175H variant with ID: {}", tp53Variant.getVariantId());
                
                log.info("Test variants generated successfully");
            } else {
                log.warn("No variant calling found with ID 1, skipping test variant generation");
            }
        };
    }
}
