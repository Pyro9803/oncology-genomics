package com.example.oncology.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "sample")
public class Sample {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_id")
    private Long sampleId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonBackReference
    private Patient patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id")
    @JsonBackReference
    private Diagnosis diagnosis;
    
    @Column(name = "sample_type", nullable = false)
    private String sampleType;
    
    @Column(name = "tissue_site")
    private String tissueSite;
    
    @Column(name = "collection_date", nullable = false)
    private LocalDate collectionDate;
    
    @Column(name = "tumor_purity", columnDefinition = "NUMERIC(5,2)")
    private BigDecimal tumorPurity;
    
    @Column(name = "sample_quality_score", columnDefinition = "NUMERIC(5,2)")
    private BigDecimal sampleQualityScore;
    
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
    
    // Relationships
    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SequencingData> sequencingData = new ArrayList<>();
    
    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<NucleicAcidExtraction> nucleicAcidExtractions = new ArrayList<>();
    
    @OneToMany(mappedBy = "tumorSample", cascade = CascadeType.ALL)
    private List<VariantCalling> tumorVariantCallings = new ArrayList<>();
    
    @OneToMany(mappedBy = "normalSample", cascade = CascadeType.ALL)
    private List<VariantCalling> normalVariantCallings = new ArrayList<>();
}
