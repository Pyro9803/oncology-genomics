package com.example.oncology.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "diagnosis")
public class Diagnosis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_id")
    private Long diagnosisId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonBackReference
    private Patient patient;
    
    @Column(name = "cancer_type", nullable = false)
    private String cancerType;
    
    @Column(name = "histology")
    private String histology;
    
    @Column(name = "t_stage")
    private String tStage;
    
    @Column(name = "n_stage")
    private String nStage;
    
    @Column(name = "m_stage")
    private String mStage;
    
    @Column(name = "stage_group")
    private String stageGroup;
    
    @Column(name = "diagnosis_date", nullable = false)
    private LocalDate diagnosisDate;
    
    @Column(name = "diagnosis_notes")
    private String diagnosisNotes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "diagnosis", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Sample> samples = new ArrayList<>();
    
    @OneToMany(mappedBy = "diagnosis", cascade = CascadeType.ALL)
    private List<ClinicalInterpretation> clinicalInterpretations = new ArrayList<>();
}
