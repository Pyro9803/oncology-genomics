package com.example.oncology.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a patient follow-up visit after treatment
 */
@Entity
@Table(name = "patient_follow_ups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientFollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followUpId;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "therapy_id")
    private TherapyRecommendation appliedTherapy;

    @Column(nullable = false)
    private LocalDate followUpDate;

    @Column(nullable = false)
    private String clinicalStatus; // IMPROVED, STABLE, PROGRESSED, RECURRED

    @Column
    private String responseAssessment; // COMPLETE_RESPONSE, PARTIAL_RESPONSE, STABLE_DISEASE, PROGRESSIVE_DISEASE

    @Column
    private String imagingResults;

    @Column
    private String laboratoryResults;

    @Column
    private Double tumorSizeChange; // Percentage change, negative for shrinkage

    @Column
    private String adverseEvents;

    @Column
    private String performanceStatus; // ECOG or Karnofsky score

    @Column
    private String qualityOfLife;

    @Column(length = 1000)
    private String clinicalNotes;

    @Column
    private Boolean requiresNewBiopsy;

    @Column
    private Boolean requiresNewSequencing;

    @Column(nullable = false)
    private String recordedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
