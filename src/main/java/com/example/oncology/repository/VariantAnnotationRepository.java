package com.example.oncology.repository;

import com.example.oncology.entity.VariantAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantAnnotationRepository extends JpaRepository<VariantAnnotation, Long> {
    
    List<VariantAnnotation> findBySomaticVariantVariantId(Long variantId);
    
    @Query("SELECT va FROM VariantAnnotation va WHERE va.somaticVariant.variantCalling.variantCallingId = :variantCallingId")
    List<VariantAnnotation> findByVariantCallingId(@Param("variantCallingId") Long variantCallingId);
    
    @Query("SELECT va FROM VariantAnnotation va WHERE va.clinicalSignificance IN ('pathogenic', 'likely_pathogenic')")
    List<VariantAnnotation> findPathogenicVariants();
    
    @Query("SELECT va FROM VariantAnnotation va WHERE va.variantEffect NOT IN ('synonymous_variant', 'intron_variant') " +
           "AND va.populationFrequency < 0.01")
    List<VariantAnnotation> findClinicallyRelevantVariants();
    
    @Query("SELECT va FROM VariantAnnotation va WHERE va.actionability IS NOT NULL")
    List<VariantAnnotation> findActionableVariants();
    
    @Query("SELECT va FROM VariantAnnotation va WHERE va.verificationStatus = :status")
    List<VariantAnnotation> findByVerificationStatus(@Param("status") String status);
}
