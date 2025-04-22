package com.example.oncology.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "follow_up")
public class FollowUp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "followup_id")
    private Long followupId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @Column(name = "followup_date", nullable = false)
    private LocalDate followupDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapy_id")
    private TherapyRecommendation therapy;
    
    @Column(name = "therapy_started_date")
    private LocalDate therapyStartedDate;
    
    @Column(name = "therapy_ended_date")
    private LocalDate therapyEndedDate;
    
    @Column(name = "response_status")
    private String responseStatus;
    
    @Column(name = "toxicity_grade")
    private String toxicityGrade;
    
    @Column(name = "toxicity_description")
    private String toxicityDescription;
    
    @Column(name = "disease_status")
    private String diseaseStatus;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "next_followup_date")
    private LocalDate nextFollowupDate;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
