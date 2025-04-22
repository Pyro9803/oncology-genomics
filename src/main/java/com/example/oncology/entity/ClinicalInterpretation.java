package com.example.oncology.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "clinical_interpretation")
public class ClinicalInterpretation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interpretation_id")
    private Long interpretationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private SomaticVariant somaticVariant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id")
    private Diagnosis diagnosis;
    
    @Column(name = "pathogenicity")
    private String pathogenicity;
    
    @Column(name = "diagnostic_significance")
    private String diagnosticSignificance;
    
    @Column(name = "prognostic_significance")
    private String prognosticSignificance;
    
    @Column(name = "therapeutic_significance")
    private String therapeuticSignificance;
    
    @Column(name = "evidence_level")
    private String evidenceLevel;
    
    @Column(name = "evidence_sources")
    private String evidenceSources;
    
    @Column(name = "interpretation_summary")
    private String interpretationSummary;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "interpreted_by")
    private String interpretedBy;
    
    @Column(name = "interpretation_date")
    private LocalDateTime interpretationDate;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "clinicalInterpretation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TherapyRecommendation> therapyRecommendations = new ArrayList<>();
}
