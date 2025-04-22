package com.example.oncology.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a clinical genomic report for a patient
 */
@Data
@Entity
@Table(name = "clinical_report")
public class ClinicalReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_calling_id", nullable = false)
    private VariantCalling variantCalling;
    
    @Column(name = "report_title", nullable = false)
    private String reportTitle;
    
    @Column(name = "report_summary", columnDefinition = "TEXT")
    private String reportSummary;
    
    @Column(name = "test_methodology", columnDefinition = "TEXT")
    private String testMethodology;
    
    @Column(name = "sequencing_details", columnDefinition = "TEXT")
    private String sequencingDetails;
    
    @Column(name = "bioinformatics_pipeline", columnDefinition = "TEXT")
    private String bioinformaticsPipeline;
    
    @Column(name = "quality_metrics", columnDefinition = "TEXT")
    private String qualityMetrics;
    
    @Column(name = "limitations", columnDefinition = "TEXT")
    private String limitations;
    
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;
    
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;
    
    @Column(name = "report_status")
    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus;
    
    @Column(name = "report_version")
    private Integer reportVersion;
    
    @Column(name = "pdf_path")
    private String pdfPath;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "reviewed_by")
    private String reviewedBy;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "review_date")
    private LocalDateTime reviewDate;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "clinicalReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportComment> reportComments = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "report_interpretation",
        joinColumns = @JoinColumn(name = "report_id"),
        inverseJoinColumns = @JoinColumn(name = "interpretation_id")
    )
    private List<ClinicalInterpretation> clinicalInterpretations = new ArrayList<>();
    
    /**
     * Status of a clinical report
     */
    public enum ReportStatus {
        DRAFT,
        UNDER_REVIEW,
        APPROVED,
        FINALIZED,
        AMENDED
    }
}
