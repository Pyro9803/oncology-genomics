package com.example.oncology.service;

import com.example.oncology.entity.Patient;
import com.example.oncology.entity.PatientFollowUp;
import com.example.oncology.entity.TherapyRecommendation;
import com.example.oncology.repository.PatientFollowUpRepository;
import com.example.oncology.repository.PatientRepository;
import com.example.oncology.repository.TherapyRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing patient follow-up records
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientFollowUpService {

    private final PatientFollowUpRepository followUpRepository;
    private final PatientRepository patientRepository;
    private final TherapyRecommendationRepository therapyRepository;

    /**
     * Create a new patient follow-up record
     *
     * @param patientId The patient ID
     * @param therapyId The therapy ID (optional, can be null if no therapy was applied)
     * @param followUpDate The date of the follow-up
     * @param clinicalStatus The clinical status (IMPROVED, STABLE, PROGRESSED, RECURRED)
     * @param responseAssessment The response assessment (COMPLETE_RESPONSE, PARTIAL_RESPONSE, STABLE_DISEASE, PROGRESSIVE_DISEASE)
     * @param imagingResults Results from imaging studies
     * @param laboratoryResults Results from laboratory tests
     * @param tumorSizeChange Percentage change in tumor size (negative for shrinkage)
     * @param adverseEvents Any adverse events or side effects
     * @param performanceStatus Patient's performance status (ECOG or Karnofsky score)
     * @param qualityOfLife Quality of life assessment
     * @param clinicalNotes Additional clinical notes
     * @param requiresNewBiopsy Whether a new biopsy is required
     * @param requiresNewSequencing Whether new sequencing is required
     * @param recordedBy The healthcare provider who recorded the follow-up
     * @return The created follow-up record
     */
    @Transactional
    public PatientFollowUp createFollowUp(
            Long patientId,
            Long therapyId,
            LocalDate followUpDate,
            String clinicalStatus,
            String responseAssessment,
            String imagingResults,
            String laboratoryResults,
            Double tumorSizeChange,
            String adverseEvents,
            String performanceStatus,
            String qualityOfLife,
            String clinicalNotes,
            Boolean requiresNewBiopsy,
            Boolean requiresNewSequencing,
            String recordedBy) {
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        
        TherapyRecommendation therapy = null;
        if (therapyId != null) {
            therapy = therapyRepository.findById(therapyId)
                    .orElseThrow(() -> new IllegalArgumentException("Therapy not found: " + therapyId));
        }
        
        PatientFollowUp followUp = new PatientFollowUp();
        followUp.setPatient(patient);
        followUp.setAppliedTherapy(therapy);
        followUp.setFollowUpDate(followUpDate);
        followUp.setClinicalStatus(clinicalStatus);
        followUp.setResponseAssessment(responseAssessment);
        followUp.setImagingResults(imagingResults);
        followUp.setLaboratoryResults(laboratoryResults);
        followUp.setTumorSizeChange(tumorSizeChange);
        followUp.setAdverseEvents(adverseEvents);
        followUp.setPerformanceStatus(performanceStatus);
        followUp.setQualityOfLife(qualityOfLife);
        followUp.setClinicalNotes(clinicalNotes);
        followUp.setRequiresNewBiopsy(requiresNewBiopsy);
        followUp.setRequiresNewSequencing(requiresNewSequencing);
        followUp.setRecordedBy(recordedBy);
        
        PatientFollowUp savedFollowUp = followUpRepository.save(followUp);
        log.info("Created follow-up record for patient {}: {}", patientId, savedFollowUp.getFollowUpId());
        
        return savedFollowUp;
    }
    
    /**
     * Update an existing follow-up record
     *
     * @param followUpId The follow-up ID
     * @param therapyId The therapy ID (optional)
     * @param clinicalStatus The clinical status
     * @param responseAssessment The response assessment
     * @param imagingResults Results from imaging studies
     * @param laboratoryResults Results from laboratory tests
     * @param tumorSizeChange Percentage change in tumor size
     * @param adverseEvents Any adverse events or side effects
     * @param performanceStatus Patient's performance status
     * @param qualityOfLife Quality of life assessment
     * @param clinicalNotes Additional clinical notes
     * @param requiresNewBiopsy Whether a new biopsy is required
     * @param requiresNewSequencing Whether new sequencing is required
     * @param recordedBy The healthcare provider who updated the follow-up
     * @return The updated follow-up record
     */
    @Transactional
    public PatientFollowUp updateFollowUp(
            Long followUpId,
            Long therapyId,
            String clinicalStatus,
            String responseAssessment,
            String imagingResults,
            String laboratoryResults,
            Double tumorSizeChange,
            String adverseEvents,
            String performanceStatus,
            String qualityOfLife,
            String clinicalNotes,
            Boolean requiresNewBiopsy,
            Boolean requiresNewSequencing,
            String recordedBy) {
        
        PatientFollowUp followUp = followUpRepository.findById(followUpId)
                .orElseThrow(() -> new IllegalArgumentException("Follow-up record not found: " + followUpId));
        
        if (therapyId != null) {
            TherapyRecommendation therapy = therapyRepository.findById(therapyId)
                    .orElseThrow(() -> new IllegalArgumentException("Therapy not found: " + therapyId));
            followUp.setAppliedTherapy(therapy);
        }
        
        if (clinicalStatus != null) followUp.setClinicalStatus(clinicalStatus);
        if (responseAssessment != null) followUp.setResponseAssessment(responseAssessment);
        if (imagingResults != null) followUp.setImagingResults(imagingResults);
        if (laboratoryResults != null) followUp.setLaboratoryResults(laboratoryResults);
        if (tumorSizeChange != null) followUp.setTumorSizeChange(tumorSizeChange);
        if (adverseEvents != null) followUp.setAdverseEvents(adverseEvents);
        if (performanceStatus != null) followUp.setPerformanceStatus(performanceStatus);
        if (qualityOfLife != null) followUp.setQualityOfLife(qualityOfLife);
        if (clinicalNotes != null) followUp.setClinicalNotes(clinicalNotes);
        if (requiresNewBiopsy != null) followUp.setRequiresNewBiopsy(requiresNewBiopsy);
        if (requiresNewSequencing != null) followUp.setRequiresNewSequencing(requiresNewSequencing);
        if (recordedBy != null) followUp.setRecordedBy(recordedBy);
        
        PatientFollowUp updatedFollowUp = followUpRepository.save(followUp);
        log.info("Updated follow-up record: {}", followUpId);
        
        return updatedFollowUp;
    }
    
    /**
     * Get all follow-up records for a patient
     *
     * @param patientId The patient ID
     * @return List of follow-up records
     */
    public List<PatientFollowUp> getPatientFollowUps(Long patientId) {
        return followUpRepository.findByPatientPatientIdOrderByFollowUpDateDesc(patientId);
    }
    
    /**
     * Get follow-up records for a patient within a date range
     *
     * @param patientId The patient ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of follow-up records
     */
    public List<PatientFollowUp> getPatientFollowUpsByDateRange(Long patientId, LocalDate startDate, LocalDate endDate) {
        return followUpRepository.findByPatientPatientIdAndFollowUpDateBetweenOrderByFollowUpDateDesc(
                patientId, startDate, endDate);
    }
    
    /**
     * Get follow-up records for a specific therapy
     *
     * @param therapyId The therapy ID
     * @return List of follow-up records
     */
    public List<PatientFollowUp> getFollowUpsByTherapy(Long therapyId) {
        return followUpRepository.findByAppliedTherapyTherapyIdOrderByFollowUpDateDesc(therapyId);
    }
    
    /**
     * Get follow-up records by clinical status
     *
     * @param patientId The patient ID
     * @param clinicalStatus The clinical status
     * @return List of follow-up records
     */
    public List<PatientFollowUp> getFollowUpsByClinicalStatus(Long patientId, String clinicalStatus) {
        return followUpRepository.findByPatientPatientIdAndClinicalStatusOrderByFollowUpDateDesc(
                patientId, clinicalStatus);
    }
    
    /**
     * Get follow-up records that require new biopsy
     *
     * @return List of follow-up records
     */
    public List<PatientFollowUp> getFollowUpsRequiringBiopsy() {
        return followUpRepository.findByRequiresNewBiopsyTrueOrderByFollowUpDateDesc();
    }
    
    /**
     * Get follow-up records that require new sequencing
     *
     * @return List of follow-up records
     */
    public List<PatientFollowUp> getFollowUpsRequiringSequencing() {
        return followUpRepository.findByRequiresNewSequencingTrueOrderByFollowUpDateDesc();
    }
    
    /**
     * Delete a follow-up record
     *
     * @param followUpId The follow-up ID
     */
    @Transactional
    public void deleteFollowUp(Long followUpId) {
        if (!followUpRepository.existsById(followUpId)) {
            throw new IllegalArgumentException("Follow-up record not found: " + followUpId);
        }
        
        followUpRepository.deleteById(followUpId);
        log.info("Deleted follow-up record: {}", followUpId);
    }
}
