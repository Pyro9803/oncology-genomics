package com.example.oncology.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing DNA/RNA extraction from a sample
 */
@Data
@Entity
@Table(name = "nucleic_acid_extraction")
public class NucleicAcidExtraction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "extraction_id")
    private Long extractionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id", nullable = false)
    @JsonBackReference
    private Sample sample;
    
    @Column(name = "extraction_type", nullable = false)
    private String extractionType; // DNA, RNA, or both
    
    @Column(name = "extraction_date", nullable = false)
    private LocalDate extractionDate;
    
    @Column(name = "extraction_method", nullable = false)
    private String extractionMethod;
    
    @Column(name = "concentration", columnDefinition = "NUMERIC(10,2)")
    private BigDecimal concentration; // ng/μL
    
    @Column(name = "purity_260_280", columnDefinition = "NUMERIC(5,2)")
    private BigDecimal purity260280; // A260/A280 ratio
    
    @Column(name = "purity_260_230", columnDefinition = "NUMERIC(5,2)")
    private BigDecimal purity260230; // A260/A230 ratio
    
    @Column(name = "quality_check_method")
    private String qualityCheckMethod; // e.g., gel electrophoresis, Bioanalyzer
    
    @Column(name = "quality_check_result")
    private String qualityCheckResult; // Pass/Fail or detailed result
    
    @Column(name = "quality_image_path")
    private String qualityImagePath; // Path to gel image or quality check result
    
    @Column(name = "storage_location")
    private String storageLocation;
    
    @Column(name = "notes")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
