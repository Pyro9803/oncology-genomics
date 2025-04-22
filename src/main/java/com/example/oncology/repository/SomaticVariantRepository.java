package com.example.oncology.repository;

import com.example.oncology.entity.SomaticVariant;
import com.example.oncology.entity.VariantCalling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SomaticVariantRepository extends JpaRepository<SomaticVariant, Long> {
    
    List<SomaticVariant> findByVariantCallingVariantCallingId(Long variantCallingId);
    
    List<SomaticVariant> findByGeneSymbol(String geneSymbol);
    
    List<SomaticVariant> findByVariantCalling(VariantCalling variantCalling);
    
    List<SomaticVariant> findByVariantType(String variantType);
    
    List<SomaticVariant> findByAlleleFrequencyBetween(BigDecimal minFrequency, BigDecimal maxFrequency);
    
    List<SomaticVariant> findByChromosomeAndPositionBetween(String chromosome, Integer startPosition, Integer endPosition);
    
    List<SomaticVariant> findByFilterStatus(String filterStatus);
    
    @Query("SELECT sv FROM SomaticVariant sv WHERE sv.variantCalling.tumorSample.patient.patientId = :patientId")
    List<SomaticVariant> findByPatientId(Long patientId);
    
    @Query("SELECT sv FROM SomaticVariant sv JOIN sv.annotation a WHERE a.impact = :impact")
    List<SomaticVariant> findByImpact(String impact);
    
    @Query("SELECT sv FROM SomaticVariant sv JOIN sv.clinicalInterpretations ci WHERE ci.pathogenicity = :pathogenicity")
    List<SomaticVariant> findByPathogenicity(String pathogenicity);
}
