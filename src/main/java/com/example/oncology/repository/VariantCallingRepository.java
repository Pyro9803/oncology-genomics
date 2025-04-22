package com.example.oncology.repository;

import com.example.oncology.entity.VariantCalling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantCallingRepository extends JpaRepository<VariantCalling, Long> {
    
    List<VariantCalling> findByTumorSampleSampleId(Long sampleId);
    
    List<VariantCalling> findByNormalSampleSampleId(Long sampleId);
    
    List<VariantCalling> findByTumorSample(com.example.oncology.entity.Sample tumorSample);
    
    List<VariantCalling> findByStatus(String status);
    
    List<VariantCalling> findByCallingMethod(String callingMethod);
    
    @Query("SELECT vc FROM VariantCalling vc WHERE vc.tumorSample.patient.patientId = :patientId")
    List<VariantCalling> findByPatientId(Long patientId);
    
    @Query("SELECT vc FROM VariantCalling vc JOIN vc.somaticVariants sv " +
           "WHERE sv.geneSymbol = :geneSymbol AND sv.filterStatus = 'PASS'")
    List<VariantCalling> findByGeneWithPassingVariants(String geneSymbol);
}
