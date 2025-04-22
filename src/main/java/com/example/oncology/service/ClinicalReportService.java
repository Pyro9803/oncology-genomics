package com.example.oncology.service;

import com.example.oncology.entity.*;
import com.example.oncology.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing clinical genomic reports
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClinicalReportService {

    private final ClinicalReportRepository reportRepository;
    private final ReportCommentRepository commentRepository;
    private final PatientRepository patientRepository;
    private final SampleRepository sampleRepository;
    private final VariantCallingRepository variantCallingRepository;
    private final ClinicalInterpretationRepository interpretationRepository;
    private final VariantAnnotationRepository annotationRepository;
    
    /**
     * Create a new clinical report
     */
    @Transactional
    public ClinicalReport createReport(
            Long patientId,
            Long sampleId,
            Long variantCallingId,
            String reportTitle,
            String reportSummary,
            String testMethodology,
            String sequencingDetails,
            String bioinformaticsPipeline,
            String qualityMetrics,
            String limitations,
            String recommendations,
            String additionalNotes,
            String createdBy) {
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("Sample not found: " + sampleId));
        
        VariantCalling variantCalling = variantCallingRepository.findById(variantCallingId)
                .orElseThrow(() -> new IllegalArgumentException("Variant calling not found: " + variantCallingId));
        
        // Create new report
        ClinicalReport report = new ClinicalReport();
        report.setPatient(patient);
        report.setSample(sample);
        report.setVariantCalling(variantCalling);
        report.setReportTitle(reportTitle);
        report.setReportSummary(reportSummary);
        report.setTestMethodology(testMethodology);
        report.setSequencingDetails(sequencingDetails);
        report.setBioinformaticsPipeline(bioinformaticsPipeline);
        report.setQualityMetrics(qualityMetrics);
        report.setLimitations(limitations);
        report.setRecommendations(recommendations);
        report.setAdditionalNotes(additionalNotes);
        report.setReportStatus(ClinicalReport.ReportStatus.DRAFT);
        report.setReportVersion(1);
        report.setCreatedBy(createdBy);
        
        return reportRepository.save(report);
    }
    
    /**
     * Add clinical interpretations to a report
     */
    @Transactional
    public ClinicalReport addInterpretationsToReport(Long reportId, List<Long> interpretationIds) {
        ClinicalReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        List<ClinicalInterpretation> interpretations = interpretationRepository.findAllById(interpretationIds);
        
        if (interpretations.size() != interpretationIds.size()) {
            throw new IllegalArgumentException("One or more interpretations not found");
        }
        
        report.getClinicalInterpretations().addAll(interpretations);
        return reportRepository.save(report);
    }
    
    /**
     * Generate a clinical report automatically based on variant annotations and interpretations
     */
    @Transactional
    public ClinicalReport generateAutomaticReport(
            Long patientId,
            Long sampleId,
            Long variantCallingId,
            String createdBy) {
        
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("Sample not found: " + sampleId));
        
        VariantCalling variantCalling = variantCallingRepository.findById(variantCallingId)
                .orElseThrow(() -> new IllegalArgumentException("Variant calling not found: " + variantCallingId));
        
        // Get all variant annotations for this variant calling
        List<VariantAnnotation> annotations = annotationRepository.findByVariantCallingId(variantCallingId);
        
        // Get all clinical interpretations for variants in this variant calling
        List<ClinicalInterpretation> interpretations = new ArrayList<>();
        for (VariantAnnotation annotation : annotations) {
            Long variantId = annotation.getSomaticVariant().getVariantId();
            interpretations.addAll(interpretationRepository.findBySomaticVariantVariantId(variantId));
        }
        
        // Generate report title
        String reportTitle = "Genomic Analysis Report for " + patient.getFirstName() + " " + patient.getLastName();
        
        // Generate report summary
        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append("This report summarizes the genomic analysis findings for patient ")
                .append(patient.getFirstName()).append(" ").append(patient.getLastName())
                .append(" (ID: ").append(patient.getPatientId()).append(").\n\n");
        
        // Add pathogenic variants to summary
        List<VariantAnnotation> pathogenicVariants = annotations.stream()
                .filter(a -> "pathogenic".equalsIgnoreCase(a.getClinicalSignificance()) || 
                             "likely_pathogenic".equalsIgnoreCase(a.getClinicalSignificance()))
                .collect(Collectors.toList());
        
        if (!pathogenicVariants.isEmpty()) {
            summaryBuilder.append("Pathogenic/Likely Pathogenic Variants:\n");
            for (VariantAnnotation variant : pathogenicVariants) {
                summaryBuilder.append("- ").append(variant.getGeneSymbol()).append(" ")
                        .append(variant.getSomaticVariant().getChromosome()).append(":")
                        .append(variant.getSomaticVariant().getPosition()).append(" ")
                        .append(variant.getSomaticVariant().getReferenceAllele()).append(">")
                        .append(variant.getSomaticVariant().getAlternateAllele())
                        .append(" (").append(variant.getVariantEffect()).append(")\n");
            }
            summaryBuilder.append("\n");
        } else {
            summaryBuilder.append("No pathogenic variants were identified in this analysis.\n\n");
        }
        
        // Add therapeutic recommendations to summary
        List<ClinicalInterpretation> therapeuticInterpretations = interpretations.stream()
                .filter(i -> i.getTherapeuticSignificance() != null && !i.getTherapeuticSignificance().isEmpty())
                .collect(Collectors.toList());
        
        if (!therapeuticInterpretations.isEmpty()) {
            summaryBuilder.append("Therapeutic Implications:\n");
            for (ClinicalInterpretation interpretation : therapeuticInterpretations) {
                summaryBuilder.append("- ").append(interpretation.getSomaticVariant().getGeneSymbol())
                        .append(": ").append(interpretation.getTherapeuticSignificance()).append("\n");
                
                // Add therapy recommendations if available
                interpretation.getTherapyRecommendations().forEach(therapy -> {
                    summaryBuilder.append("  * ").append(therapy.getTherapyName())
                            .append(" (").append(therapy.getEvidenceLevel()).append(")\n");
                });
            }
            summaryBuilder.append("\n");
        } else {
            summaryBuilder.append("No variants with therapeutic implications were identified.\n\n");
        }
        
        // Generate test methodology
        String testMethodology = "Next-Generation Sequencing (NGS) was performed on genomic DNA extracted from the patient's tumor sample. " +
                "The sequencing was performed using " + variantCalling.getPipelineVersion() + " with " + 
                variantCalling.getReferenceGenome() + " as the reference genome.";
        
        // Generate sequencing details
        String sequencingDetails = "The sequencing was performed on " + sample.getSampleType() + " tissue. " +
                "The sample was processed according to standard laboratory protocols.";
        
        // Generate bioinformatics pipeline
        String bioinformaticsPipeline = "Variant calling was performed using " + variantCalling.getCallingMethod() + ". " +
                "Variants were annotated using multiple databases including dbSNP, COSMIC, ClinVar, and gnomAD.";
        
        // Generate quality metrics
        String qualityMetrics = "Total variants called: " + (variantCalling.getTotalVariantsCalled() != null ? variantCalling.getTotalVariantsCalled() : "N/A") + "\n" +
                "Variants passed filter: " + (variantCalling.getVariantsPassedFilter() != null ? variantCalling.getVariantsPassedFilter() : "N/A");
        
        // Generate limitations
        String limitations = "This test was designed to detect somatic variants in the coding regions of the analyzed genes. " +
                "It may not detect large genomic rearrangements, deep intronic variants, or variants in regions of high sequence complexity. " +
                "The absence of a reportable alteration does not preclude the presence of a variant below the limit of detection of this assay.";
        
        // Generate recommendations
        StringBuilder recommendationsBuilder = new StringBuilder();
        if (!therapeuticInterpretations.isEmpty()) {
            recommendationsBuilder.append("Based on the identified genomic alterations, the following therapeutic options may be considered:\n");
            for (ClinicalInterpretation interpretation : therapeuticInterpretations) {
                interpretation.getTherapyRecommendations().forEach(therapy -> {
                    recommendationsBuilder.append("- ").append(therapy.getTherapyName())
                            .append(" (").append(therapy.getEvidenceLevel()).append("): ")
                            .append(therapy.getRecommendationSummary()).append("\n");
                });
            }
        } else {
            recommendationsBuilder.append("No specific therapy recommendations based on genomic alterations. " +
                    "Standard of care treatment should be considered.");
        }
        
        // Create the report
        ClinicalReport report = new ClinicalReport();
        report.setPatient(patient);
        report.setSample(sample);
        report.setVariantCalling(variantCalling);
        report.setReportTitle(reportTitle);
        report.setReportSummary(summaryBuilder.toString());
        report.setTestMethodology(testMethodology);
        report.setSequencingDetails(sequencingDetails);
        report.setBioinformaticsPipeline(bioinformaticsPipeline);
        report.setQualityMetrics(qualityMetrics);
        report.setLimitations(limitations);
        report.setRecommendations(recommendationsBuilder.toString());
        report.setReportStatus(ClinicalReport.ReportStatus.DRAFT);
        report.setReportVersion(1);
        report.setCreatedBy(createdBy);
        report.setClinicalInterpretations(interpretations);
        
        return reportRepository.save(report);
    }
    
    /**
     * Submit a report for review
     */
    @Transactional
    public ClinicalReport submitReportForReview(Long reportId) {
        ClinicalReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        if (report.getReportStatus() != ClinicalReport.ReportStatus.DRAFT) {
            throw new IllegalStateException("Report is not in DRAFT status");
        }
        
        report.setReportStatus(ClinicalReport.ReportStatus.UNDER_REVIEW);
        return reportRepository.save(report);
    }
    
    /**
     * Add a comment to a report
     */
    @Transactional
    public ReportComment addCommentToReport(
            Long reportId,
            String commentText,
            String commentSection,
            ReportComment.CommentType commentType,
            String commentedBy) {
        
        ClinicalReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        ReportComment comment = new ReportComment();
        comment.setClinicalReport(report);
        comment.setCommentText(commentText);
        comment.setCommentSection(commentSection);
        comment.setCommentType(commentType);
        comment.setCommentedBy(commentedBy);
        comment.setResolved(false);
        
        return commentRepository.save(comment);
    }
    
    /**
     * Resolve a report comment
     */
    @Transactional
    public ReportComment resolveComment(
            Long commentId,
            String resolvedBy,
            String resolutionNotes) {
        
        ReportComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        
        comment.setResolved(true);
        comment.setResolvedBy(resolvedBy);
        comment.setResolutionDate(LocalDateTime.now());
        comment.setResolutionNotes(resolutionNotes);
        
        return commentRepository.save(comment);
    }
    
    /**
     * Approve a report
     */
    @Transactional
    public ClinicalReport approveReport(Long reportId, String approvedBy) {
        ClinicalReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        if (report.getReportStatus() != ClinicalReport.ReportStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Report is not in UNDER_REVIEW status");
        }
        
        // Check if all comments are resolved
        List<ReportComment> unresolvedComments = commentRepository.findByClinicalReportReportIdAndResolved(reportId, false);
        if (!unresolvedComments.isEmpty()) {
            throw new IllegalStateException("Report has unresolved comments");
        }
        
        report.setReportStatus(ClinicalReport.ReportStatus.APPROVED);
        report.setApprovedBy(approvedBy);
        report.setApprovalDate(LocalDateTime.now());
        
        return reportRepository.save(report);
    }
    
    /**
     * Finalize a report
     */
    @Transactional
    public ClinicalReport finalizeReport(Long reportId, String pdfPath) {
        ClinicalReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        if (report.getReportStatus() != ClinicalReport.ReportStatus.APPROVED) {
            throw new IllegalStateException("Report is not in APPROVED status");
        }
        
        report.setReportStatus(ClinicalReport.ReportStatus.FINALIZED);
        report.setPdfPath(pdfPath);
        
        return reportRepository.save(report);
    }
    
    /**
     * Amend a report
     */
    @Transactional
    public ClinicalReport amendReport(Long reportId, String createdBy) {
        ClinicalReport originalReport = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        if (originalReport.getReportStatus() != ClinicalReport.ReportStatus.FINALIZED) {
            throw new IllegalStateException("Only finalized reports can be amended");
        }
        
        // Create a new version of the report
        ClinicalReport amendedReport = new ClinicalReport();
        amendedReport.setPatient(originalReport.getPatient());
        amendedReport.setSample(originalReport.getSample());
        amendedReport.setVariantCalling(originalReport.getVariantCalling());
        amendedReport.setReportTitle(originalReport.getReportTitle() + " (Amended)");
        amendedReport.setReportSummary(originalReport.getReportSummary());
        amendedReport.setTestMethodology(originalReport.getTestMethodology());
        amendedReport.setSequencingDetails(originalReport.getSequencingDetails());
        amendedReport.setBioinformaticsPipeline(originalReport.getBioinformaticsPipeline());
        amendedReport.setQualityMetrics(originalReport.getQualityMetrics());
        amendedReport.setLimitations(originalReport.getLimitations());
        amendedReport.setRecommendations(originalReport.getRecommendations());
        amendedReport.setAdditionalNotes("This is an amended report. Original report ID: " + reportId);
        amendedReport.setReportStatus(ClinicalReport.ReportStatus.DRAFT);
        amendedReport.setReportVersion(originalReport.getReportVersion() + 1);
        amendedReport.setCreatedBy(createdBy);
        amendedReport.setClinicalInterpretations(new ArrayList<>(originalReport.getClinicalInterpretations()));
        
        return reportRepository.save(amendedReport);
    }
    
    /**
     * Get all reports for a patient
     */
    public List<ClinicalReport> getReportsByPatient(Long patientId) {
        return reportRepository.findByPatientPatientId(patientId);
    }
    
    /**
     * Get the latest report for a patient
     */
    public ClinicalReport getLatestReportForPatient(Long patientId) {
        List<ClinicalReport> reports = reportRepository.findLatestReportsByPatient(patientId);
        return reports.isEmpty() ? null : reports.get(0);
    }
    
    /**
     * Get all comments for a report
     */
    public List<ReportComment> getCommentsForReport(Long reportId) {
        return commentRepository.findByClinicalReportReportId(reportId);
    }
    
    /**
     * Get unresolved comments for a report
     */
    public List<ReportComment> getUnresolvedCommentsForReport(Long reportId) {
        return commentRepository.findByClinicalReportReportIdAndResolved(reportId, false);
    }
    
    /**
     * Get a report by ID
     */
    public ClinicalReport getReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
    }
}
