package com.example.oncology.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "annotation")
public class Annotation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "annotation_id")
    private Long annotationId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private SomaticVariant somaticVariant;
    
    @Column(name = "gene", nullable = false)
    private String gene;
    
    @Column(name = "consequence")
    private String consequence;
    
    @Column(name = "impact")
    private String impact;
    
    @Column(name = "exon")
    private String exon;
    
    @Column(name = "intron")
    private String intron;
    
    @Column(name = "protein_position")
    private String proteinPosition;
    
    @Column(name = "amino_acid_change")
    private String aminoAcidChange;
    
    @Column(name = "codon_change")
    private String codonChange;
    
    @Column(name = "dbsnp_id")
    private String dbsnpId;
    
    @Column(name = "cosmic_id")
    private String cosmicId;
    
    @Column(name = "gnomad_af", columnDefinition = "NUMERIC(10,9)")
    private BigDecimal gnomadAf;
    
    @Column(name = "clinvar_id")
    private String clinvarId;
    
    @Column(name = "clinvar_significance")
    private String clinvarSignificance;
    
    @Column(name = "oncokb_variant_id")
    private String oncokbVariantId;
    
    @Column(name = "civic_variant_id")
    private String civicVariantId;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
