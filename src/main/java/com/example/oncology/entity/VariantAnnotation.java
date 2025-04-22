package com.example.oncology.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing variant annotations from various databases
 */
@Data
@Entity
@Table(name = "variant_annotation")
public class VariantAnnotation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "annotation_id")
    private Long annotationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    @JsonBackReference
    private SomaticVariant somaticVariant;
    
    @Column(name = "gene_symbol")
    private String geneSymbol;
    
    @Column(name = "gene_id")
    private String geneId;
    
    @Column(name = "transcript_id")
    private String transcriptId;
    
    @Column(name = "variant_type")
    private String variantType; // SNV, insertion, deletion, etc.
    
    @Column(name = "variant_effect")
    private String variantEffect; // missense, nonsense, frameshift, etc.
    
    @Column(name = "amino_acid_change")
    private String aminoAcidChange; // e.g., p.V600E
    
    @Column(name = "cdna_change")
    private String cdnaChange; // e.g., c.1799T>A
    
    @Column(name = "exon_number")
    private Integer exonNumber;
    
    @Column(name = "population_frequency")
    private Double populationFrequency;
    
    @Column(name = "dbsnp_id")
    private String dbsnpId;
    
    @Column(name = "cosmic_id")
    private String cosmicId;
    
    @Column(name = "clinvar_id")
    private String clinvarId;
    
    @Column(name = "clinvar_significance")
    private String clinvarSignificance;
    
    @Column(name = "hgmd_id")
    private String hgmdId;
    
    @Column(name = "civic_id")
    private String civicId;
    
    @Column(name = "oncokb_id")
    private String oncokbId;
    
    @Column(name = "clinical_significance")
    private String clinicalSignificance; // pathogenic, likely pathogenic, VUS, etc.
    
    @Column(name = "oncogenic_effect")
    private String oncogenicEffect; // oncogenic, likely oncogenic, etc.
    
    @Column(name = "actionability")
    private String actionability; // diagnostic, prognostic, therapeutic, etc.
    
    @Column(name = "drug_associations", columnDefinition = "text")
    private String drugAssociations;
    
    @Column(name = "literature_references", columnDefinition = "text")
    private String literatureReferences;
    
    @Column(name = "verification_status")
    private String verificationStatus; // PENDING, VERIFIED, FAILED
    
    @Column(name = "verification_method")
    private String verificationMethod; // Sanger, ddPCR, etc.
    
    @Column(name = "verification_notes")
    private String verificationNotes;
    
    @Column(name = "annotation_source")
    private String annotationSource; // VEP, ANNOVAR, SnpEff, etc.
    
    @Column(name = "annotation_version")
    private String annotationVersion;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
