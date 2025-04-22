package com.example.oncology.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
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
    @JsonBackReference
    private Sample sample;
    
    @Column(name = "platform", nullable = false)
    private String platform;
    
    @Column(name = "library_prep_kit", nullable = false)
    private String libraryPrepKit;
    
    @Column(name = "library_prep_date")
    private LocalDate libraryPrepDate;
    
    @Column(name = "library_id")
    private String libraryId;
    
    @Column(name = "library_concentration", columnDefinition = "NUMERIC(10,2)")
    private BigDecimal libraryConcentration;
    
    @Column(name = "library_size_distribution")
    private String librarySizeDistribution;
    
    @Column(name = "sequencing_type", nullable = false)
    private String sequencingType;
    
    @Column(name = "target_coverage")
    private Integer targetCoverage;
    
    @Column(name = "mean_coverage", columnDefinition = "NUMERIC(10,2)")
    private BigDecimal meanCoverage;
    
    @Column(name = "sequencing_date")
    private LocalDate sequencingDate;
    
    @Column(name = "fastq_path")
    private String fastqPath;
    
    @Column(name = "bam_path")
    private String bamPath;
    
    @Column(name = "quality_metrics", columnDefinition = "jsonb")
    private String qualityMetrics;
    
    @Column(name = "run_id")
    private String runId;
    
    @Column(name = "flow_cell_id")
    private String flowCellId;
    
    @Column(name = "lane_number")
    private Integer laneNumber;
    
    @Column(name = "index_sequence")
    private String indexSequence;
    
    @Column(name = "read_length")
    private String readLength;
    
    @Column(name = "total_reads")
    private Long totalReads;
    
    @Column(name = "percent_q30", columnDefinition = "NUMERIC(5,2)")
    private BigDecimal percentQ30;
    
    @Column(name = "status")
    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
