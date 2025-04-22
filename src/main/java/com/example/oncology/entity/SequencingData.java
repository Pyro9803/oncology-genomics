package com.example.oncology.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sequencing_data")
public class SequencingData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sequencing_id")
    private Long sequencingId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;
    
    @Column(name = "platform", nullable = false)
    private String platform;
    
    @Column(name = "library_prep")
    private String libraryPrep;
    
    @Column(name = "sequencing_type", nullable = false)
    private String sequencingType;
    
    @Column(name = "target_coverage")
    private Integer targetCoverage;
    
    @Column(name = "mean_coverage", precision = 10, scale = 2)
    private Double meanCoverage;
    
    @Column(name = "sequencing_date")
    private LocalDate sequencingDate;
    
    @Column(name = "fastq_path")
    private String fastqPath;
    
    @Column(name = "bam_path")
    private String bamPath;
    
    @Column(name = "quality_metrics", columnDefinition = "jsonb")
    private String qualityMetrics;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
