package com.example.oncology.service;

import com.example.oncology.entity.FollowUp;
import com.example.oncology.entity.Patient;
import com.example.oncology.entity.TherapyRecommendation;
import com.example.oncology.repository.FollowUpRepository;
import com.example.oncology.repository.PatientRepository;
import com.example.oncology.repository.TherapyRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing patient follow-ups
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final PatientRepository patientRepository;
    private final TherapyRecommendationRepository therapyRecommendationRepository;
    
    /**
     * Record a new follow-up for a patient
     * @param patientId ID of the patient
     * @param followupDate Date of the follow-up
     * @param therapyId ID of the therapy being evaluated (optional)
     * @param therapyStartedDate Date therapy started (optional)
     * @param therapyEndedDate Date therapy ended (optional)
     * @param responseStatus Response status (e.g., CR, PR, SD, PD)
     * @param toxicityGrade Toxicity grade
     * @param toxicityDescription Description of toxicity
     * @param diseaseStatus Disease status (e.g., NED, AWD, DOD)
     * @param notes Additional notes
     * @param nextFollowupDate Date of next follow-up
     * @return The created follow-up record
     */
    @Transactional
    public FollowUp recordFollowUp(
            Long patientId,
            LocalDate followupDate,
            Long therapyId,
            LocalDate therapyStartedDate,
            LocalDate therapyEndedDate,
            String responseStatus,
            String toxicityGrade,
            String toxicityDescription,
            String diseaseStatus,
            String notes,
            LocalDate nextFollowupDate) {
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        
        TherapyRecommendation therapy = null;
        if (therapyId != null) {
            therapy = therapyRecommendationRepository.findById(therapyId)
                    .orElseThrow(() -> new IllegalArgumentException("Therapy recommendation not found: " + therapyId));
        }
        
        FollowUp followUp = new FollowUp();
        followUp.setPatient(patient);
        followUp.setFollowupDate(followupDate);
        followUp.setTherapy(therapy);
        followUp.setTherapyStartedDate(therapyStartedDate);
        followUp.setTherapyEndedDate(therapyEndedDate);
        followUp.setResponseStatus(responseStatus);
        followUp.setToxicityGrade(toxicityGrade);
        followUp.setToxicityDescription(toxicityDescription);
        followUp.setDiseaseStatus(diseaseStatus);
        followUp.setNotes(notes);
        followUp.setNextFollowupDate(nextFollowupDate);
        
        return followUpRepository.save(followUp);
    }
    
    /**
     * Get all follow-ups for a patient
     * @param patientId ID of the patient
     * @return List of follow-ups
     */
    public List<FollowUp> getPatientFollowUps(Long patientId) {
        return followUpRepository.findByPatientPatientId(patientId);
    }
    
    /**
     * Get follow-ups for a specific therapy
     * @param therapyId ID of the therapy
     * @return List of follow-ups
     */
    public List<FollowUp> getTherapyFollowUps(Long therapyId) {
        return followUpRepository.findByTherapyTherapyId(therapyId);
    }
    
    /**
     * Get follow-ups by response status
     * @param responseStatus Response status (e.g., CR, PR, SD, PD)
     * @return List of follow-ups
     */
    public List<FollowUp> getFollowUpsByResponseStatus(String responseStatus) {
        return followUpRepository.findByResponseStatus(responseStatus);
    }
    
    /**
     * Get follow-ups by disease status
     * @param diseaseStatus Disease status (e.g., NED, AWD, DOD)
     * @return List of follow-ups
     */
    public List<FollowUp> getFollowUpsByDiseaseStatus(String diseaseStatus) {
        return followUpRepository.findByDiseaseStatus(diseaseStatus);
    }
    
    /**
     * Get follow-ups within a date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of follow-ups
     */
    public List<FollowUp> getFollowUpsInDateRange(LocalDate startDate, LocalDate endDate) {
        return followUpRepository.findByFollowupDateBetween(startDate, endDate);
    }
    
    /**
     * Get follow-ups for patients with a specific cancer type
     * @param cancerType Type of cancer
     * @return List of follow-ups
     */
    public List<FollowUp> getFollowUpsByCancerType(String cancerType) {
        return followUpRepository.findByCancerType(cancerType);
    }
    
    /**
     * Get follow-ups for patients with variants in a specific gene
     * @param geneSymbol Gene symbol
     * @return List of follow-ups
     */
    public List<FollowUp> getFollowUpsByGene(String geneSymbol) {
        return followUpRepository.findByGene(geneSymbol);
    }
}
