-- Patient information
CREATE TABLE IF NOT EXISTS patient (
    patient_id SERIAL PRIMARY KEY,
    medical_record_number VARCHAR(50) UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10),
    contact_number VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Diagnosis information
CREATE TABLE IF NOT EXISTS diagnosis (
    diagnosis_id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL REFERENCES patient(patient_id) ON DELETE CASCADE,
    cancer_type VARCHAR(100) NOT NULL,
    histology VARCHAR(100),
    t_stage VARCHAR(10),
    n_stage VARCHAR(10),
    m_stage VARCHAR(10),
    stage_group VARCHAR(10),
    diagnosis_date DATE NOT NULL,
    diagnosis_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sample information
CREATE TABLE IF NOT EXISTS sample (
    sample_id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL REFERENCES patient(patient_id) ON DELETE CASCADE,
    diagnosis_id INT REFERENCES diagnosis(diagnosis_id),
    sample_type VARCHAR(50) NOT NULL, -- tumor, normal, etc.
    tissue_site VARCHAR(100),
    collection_date DATE NOT NULL,
    tumor_purity DECIMAL(5,2), -- percentage of tumor cells in sample
    sample_quality_score DECIMAL(5,2), -- quality metric
    storage_location VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sequencing data
CREATE TABLE IF NOT EXISTS sequencing_data (
    sequencing_id SERIAL PRIMARY KEY,
    sample_id INT NOT NULL REFERENCES sample(sample_id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL, -- e.g., Illumina NovaSeq
    library_prep VARCHAR(100), -- library preparation method
    sequencing_type VARCHAR(50) NOT NULL, -- WGS, WES, panel
    target_coverage INT, -- target depth of coverage
    mean_coverage DECIMAL(10,2), -- actual mean coverage achieved
    sequencing_date DATE,
    fastq_path TEXT, -- path to raw FASTQ files
    bam_path TEXT, -- path to aligned BAM file
    quality_metrics JSONB, -- JSON with various QC metrics
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Variant calling runs
CREATE TABLE IF NOT EXISTS variant_calling (
    variant_calling_id SERIAL PRIMARY KEY,
    tumor_sample_id INT NOT NULL REFERENCES sample(sample_id),
    normal_sample_id INT REFERENCES sample(sample_id), -- can be NULL for tumor-only analysis
    pipeline_version VARCHAR(50) NOT NULL, -- e.g., GATK 4.2.0
    reference_genome VARCHAR(20) NOT NULL, -- e.g., GRCh38
    calling_method VARCHAR(50) NOT NULL, -- e.g., Mutect2
    panel_of_normals_used BOOLEAN DEFAULT FALSE,
    dbsnp_version VARCHAR(20),
    gnomad_version VARCHAR(20),
    filters_applied TEXT,
    vcf_output_path TEXT,
    status VARCHAR(20) NOT NULL, -- PENDING, RUNNING, COMPLETED, FAILED
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    error_message TEXT,
    progress_log TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Somatic variants
CREATE TABLE IF NOT EXISTS somatic_variant (
    variant_id SERIAL PRIMARY KEY,
    variant_calling_id INT NOT NULL REFERENCES variant_calling(variant_calling_id) ON DELETE CASCADE,
    chromosome VARCHAR(10) NOT NULL,
    position INT NOT NULL,
    reference_allele TEXT NOT NULL,
    alternate_allele TEXT NOT NULL,
    variant_type VARCHAR(20) NOT NULL, -- SNV, insertion, deletion, etc.
    gene_symbol VARCHAR(50),
    transcript_id VARCHAR(50),
    hgvs_c VARCHAR(100), -- coding change
    hgvs_p VARCHAR(100), -- protein change
    allele_frequency DECIMAL(10,5), -- VAF in tumor
    read_depth INT, -- total read depth at position
    alt_read_count INT, -- reads supporting alternate allele
    filter_status VARCHAR(20) NOT NULL, -- PASS or reason for filtering
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Variant annotations
CREATE TABLE IF NOT EXISTS annotation (
    annotation_id SERIAL PRIMARY KEY,
    variant_id INT NOT NULL REFERENCES somatic_variant(variant_id) ON DELETE CASCADE,
    gene VARCHAR(50) NOT NULL,
    consequence VARCHAR(50), -- e.g., missense, frameshift
    impact VARCHAR(20), -- HIGH, MODERATE, LOW, MODIFIER
    exon VARCHAR(20),
    intron VARCHAR(20),
    protein_position VARCHAR(20),
    amino_acid_change VARCHAR(50),
    codon_change VARCHAR(50),
    dbsnp_id VARCHAR(20),
    cosmic_id VARCHAR(50),
    gnomad_af DECIMAL(10,9), -- allele frequency in gnomAD
    clinvar_id VARCHAR(20),
    clinvar_significance VARCHAR(50),
    oncokb_variant_id VARCHAR(50),
    civic_variant_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Clinical interpretation of variants
CREATE TABLE IF NOT EXISTS clinical_interpretation (
    interpretation_id SERIAL PRIMARY KEY,
    variant_id INT NOT NULL REFERENCES somatic_variant(variant_id) ON DELETE CASCADE,
    diagnosis_id INT REFERENCES diagnosis(diagnosis_id),
    pathogenicity VARCHAR(50), -- Pathogenic, Likely Pathogenic, etc.
    diagnostic_significance TEXT, -- significance for diagnosis
    prognostic_significance TEXT, -- significance for prognosis
    therapeutic_significance TEXT, -- significance for treatment
    evidence_level VARCHAR(20), -- level of evidence
    evidence_sources TEXT, -- sources of evidence
    interpretation_summary TEXT,
    notes TEXT,
    interpreted_by VARCHAR(100),
    interpretation_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Therapy recommendations
CREATE TABLE IF NOT EXISTS therapy_recommendation (
    therapy_id SERIAL PRIMARY KEY,
    interpretation_id INT NOT NULL REFERENCES clinical_interpretation(interpretation_id) ON DELETE CASCADE,
    therapy_name VARCHAR(100) NOT NULL,
    therapy_type VARCHAR(50), -- targeted, immunotherapy, etc.
    evidence_level VARCHAR(20), -- level of evidence
    evidence_source VARCHAR(100), -- e.g., FDA, NCCN, clinical trial
    reference_id VARCHAR(100), -- e.g., NCT number, publication ID
    recommendation_summary TEXT,
    contraindications TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Patient follow-up records
CREATE TABLE IF NOT EXISTS patient_follow_ups (
    follow_up_id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL REFERENCES patient(patient_id) ON DELETE CASCADE,
    therapy_id INT REFERENCES therapy_recommendation(therapy_id),
    follow_up_date DATE NOT NULL,
    clinical_status VARCHAR(50) NOT NULL, -- IMPROVED, STABLE, PROGRESSED, RECURRED
    response_assessment VARCHAR(50), -- COMPLETE_RESPONSE, PARTIAL_RESPONSE, STABLE_DISEASE, PROGRESSIVE_DISEASE
    imaging_results TEXT,
    laboratory_results TEXT,
    tumor_size_change DECIMAL(8,2), -- Percentage change, negative for shrinkage
    adverse_events TEXT,
    performance_status VARCHAR(50), -- ECOG or Karnofsky score
    quality_of_life TEXT,
    clinical_notes TEXT,
    requires_new_biopsy BOOLEAN DEFAULT FALSE,
    requires_new_sequencing BOOLEAN DEFAULT FALSE,
    recorded_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Clinical reports
CREATE TABLE IF NOT EXISTS clinical_report (
    report_id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL REFERENCES patient(patient_id) ON DELETE CASCADE,
    sample_id INT NOT NULL REFERENCES sample(sample_id),
    variant_calling_id INT NOT NULL REFERENCES variant_calling(variant_calling_id),
    report_title VARCHAR(200) NOT NULL,
    report_summary TEXT,
    test_methodology TEXT,
    sequencing_details TEXT,
    bioinformatics_pipeline TEXT,
    quality_metrics TEXT,
    limitations TEXT,
    recommendations TEXT,
    additional_notes TEXT,
    report_status VARCHAR(50) NOT NULL, -- DRAFT, REVIEW, APPROVED, FINALIZED
    report_version INT DEFAULT 1,
    pdf_path VARCHAR(255),
    created_by VARCHAR(100) NOT NULL,
    reviewed_by VARCHAR(100),
    approved_by VARCHAR(100),
    finalized_by VARCHAR(100),
    review_date TIMESTAMP,
    approval_date TIMESTAMP,
    finalization_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Report comments
CREATE TABLE IF NOT EXISTS report_comment (
    comment_id SERIAL PRIMARY KEY,
    report_id INT NOT NULL REFERENCES clinical_report(report_id) ON DELETE CASCADE,
    comment_text TEXT NOT NULL,
    comment_section VARCHAR(100),
    comment_type VARCHAR(50), -- SUGGESTION, CORRECTION, QUESTION
    commented_by VARCHAR(100) NOT NULL,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_by VARCHAR(100),
    resolution_date TIMESTAMP,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for frequently queried columns
CREATE INDEX idx_patient_name ON patient(last_name, first_name);
CREATE INDEX idx_sample_patient ON sample(patient_id);
CREATE INDEX idx_variant_gene ON somatic_variant(gene_symbol);
CREATE INDEX idx_variant_chromosome_position ON somatic_variant(chromosome, position);
CREATE INDEX idx_variant_calling_status ON variant_calling(status);
CREATE INDEX idx_followup_patient ON patient_follow_ups(patient_id);
CREATE INDEX idx_clinical_report_patient ON clinical_report(patient_id);
CREATE INDEX idx_report_comment_report ON report_comment(report_id);
