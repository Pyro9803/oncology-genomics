package com.example.oncology.repository;

import com.example.oncology.entity.FollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
    
    List<FollowUp> findByPatientPatientId(Long patientId);
    
    List<FollowUp> findByTherapyTherapyId(Long therapyId);
    
    List<FollowUp> findByResponseStatus(String responseStatus);
    
    List<FollowUp> findByDiseaseStatus(String diseaseStatus);
    
    List<FollowUp> findByFollowupDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT f FROM FollowUp f WHERE f.therapy.therapyName = :therapyName")
    List<FollowUp> findByTherapyName(String therapyName);
    
    @Query("SELECT f FROM FollowUp f " +
           "JOIN f.therapy tr " +
           "JOIN tr.clinicalInterpretation ci " +
           "JOIN ci.somaticVariant sv " +
           "WHERE sv.geneSymbol = :geneSymbol")
    List<FollowUp> findByGene(String geneSymbol);
    
    @Query("SELECT f FROM FollowUp f " +
           "JOIN f.patient p " +
           "JOIN p.diagnoses d " +
           "WHERE d.cancerType = :cancerType")
    List<FollowUp> findByCancerType(String cancerType);
}
