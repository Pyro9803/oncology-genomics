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
@Table(name = "variant_calling")
public class VariantCalling {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_calling_id")
    private Long variantCallingId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tumor_sample_id", nullable = false)
    private Sample tumorSample;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "normal_sample_id")
    private Sample normalSample;
    
    @Column(name = "pipeline_version", nullable = false)
    private String pipelineVersion;
    
    @Column(name = "reference_genome", nullable = false)
    private String referenceGenome;
    
    @Column(name = "calling_method", nullable = false)
    private String callingMethod;
    
    @Column(name = "panel_of_normals_used")
    private Boolean panelOfNormalsUsed;
    
    @Column(name = "dbsnp_version")
    private String dbsnpVersion;
    
    @Column(name = "gnomad_version")
    private String gnomadVersion;
    
    @Column(name = "filters_applied")
    private String filtersApplied;
    
    @Column(name = "vcf_output_path")
    private String vcfOutputPath;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "progress_log")
    private String progressLog;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "variantCalling", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SomaticVariant> somaticVariants = new ArrayList<>();
}
