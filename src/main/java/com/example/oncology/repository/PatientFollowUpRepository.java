package com.example.oncology.repository;

import com.example.oncology.entity.PatientFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing patient follow-up records
 */
@Repository
public interface PatientFollowUpRepository extends JpaRepository<PatientFollowUp, Long> {
    
    /**
     * Find all follow-ups for a specific patient
     * 
     * @param patientId The patient ID
     * @return List of follow-up records
     */
    List<PatientFollowUp> findByPatientPatientIdOrderByFollowUpDateDesc(Long patientId);
    
    /**
     * Find follow-ups for a specific patient within a date range
     * 
     * @param patientId The patient ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of follow-up records
     */
    List<PatientFollowUp> findByPatientPatientIdAndFollowUpDateBetweenOrderByFollowUpDateDesc(
            Long patientId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find follow-ups for a specific therapy
     * 
     * @param therapyId The therapy ID
     * @return List of follow-up records
     */
    List<PatientFollowUp> findByAppliedTherapyTherapyIdOrderByFollowUpDateDesc(Long therapyId);
    
    /**
     * Find follow-ups by clinical status
     * 
     * @param patientId The patient ID
     * @param clinicalStatus The clinical status
     * @return List of follow-up records
     */
    List<PatientFollowUp> findByPatientPatientIdAndClinicalStatusOrderByFollowUpDateDesc(
            Long patientId, String clinicalStatus);
    
    /**
     * Find follow-ups that require new biopsy
     * 
     * @return List of follow-up records
     */
    List<PatientFollowUp> findByRequiresNewBiopsyTrueOrderByFollowUpDateDesc();
    
    /**
     * Find follow-ups that require new sequencing
     * 
     * @return List of follow-up records
     */
    List<PatientFollowUp> findByRequiresNewSequencingTrueOrderByFollowUpDateDesc();
}
