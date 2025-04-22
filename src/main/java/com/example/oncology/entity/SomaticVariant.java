package com.example.oncology.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "somatic_variant")
public class SomaticVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_calling_id", nullable = false)
    @JsonBackReference
    private VariantCalling variantCalling;
    
    @Column(name = "chromosome", nullable = false)
    private String chromosome;
    
    @Column(name = "position", nullable = false)
    private Integer position;
    
    @Column(name = "reference_allele", nullable = false)
    private String referenceAllele;
    
    @Column(name = "alternate_allele", nullable = false)
    private String alternateAllele;
    
    @Column(name = "variant_type", nullable = false)
    private String variantType;
    
    @Column(name = "gene_symbol")
    private String geneSymbol;
    
    @Column(name = "transcript_id")
    private String transcriptId;
    
    @Column(name = "hgvs_c")
    private String hgvsC;
    
    @Column(name = "hgvs_p")
    private String hgvsP;
    
    @Column(name = "allele_frequency", columnDefinition = "NUMERIC(10,5)")
    private BigDecimal alleleFrequency;
    
    @Column(name = "read_depth")
    private Integer readDepth;
    
    @Column(name = "alt_read_count")
    private Integer altReadCount;
    
    @Column(name = "filter_status", nullable = false)
    private String filterStatus;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToOne(mappedBy = "somaticVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Annotation annotation;
    
    @OneToMany(mappedBy = "somaticVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClinicalInterpretation> clinicalInterpretations = new ArrayList<>();
}
