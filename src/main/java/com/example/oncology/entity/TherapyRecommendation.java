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
@Table(name = "therapy_recommendation")
public class TherapyRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "therapy_id")
    private Long therapyId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interpretation_id", nullable = false)
    private ClinicalInterpretation clinicalInterpretation;
    
    @Column(name = "therapy_name", nullable = false)
    private String therapyName;
    
    @Column(name = "therapy_type")
    private String therapyType;
    
    @Column(name = "evidence_level")
    private String evidenceLevel;
    
    @Column(name = "evidence_source")
    private String evidenceSource;
    
    @Column(name = "reference_id")
    private String referenceId;
    
    @Column(name = "recommendation_summary")
    private String recommendationSummary;
    
    @Column(name = "contraindications")
    private String contraindications;
    
    @Column(name = "notes")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "therapy", cascade = CascadeType.ALL)
    private List<FollowUp> followUps = new ArrayList<>();
}
