package com.example.oncology.repository;

import com.example.oncology.entity.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    
    List<Annotation> findBySomaticVariantVariantId(Long variantId);
    
    List<Annotation> findByGene(String gene);
    
    List<Annotation> findByConsequence(String consequence);
    
    List<Annotation> findByImpact(String impact);
    
    List<Annotation> findByDbsnpId(String dbsnpId);
    
    List<Annotation> findByCosmicId(String cosmicId);
    
    List<Annotation> findByClinvarSignificance(String clinvarSignificance);
    
    @Query("SELECT a FROM Annotation a WHERE a.gnomadAf <= :maxFrequency")
    List<Annotation> findByMaxGnomadFrequency(Double maxFrequency);
    
    @Query("SELECT a FROM Annotation a WHERE a.somaticVariant.variantCalling.tumorSample.patient.patientId = :patientId")
    List<Annotation> findByPatientId(Long patientId);
}
