package com.example.oncology.controller;

import com.example.oncology.entity.ClinicalReport;
import com.example.oncology.entity.ReportComment;
import com.example.oncology.service.ClinicalReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for clinical report operations
 */
@RestController
@RequestMapping("/clinical-reports")
@RequiredArgsConstructor
public class ClinicalReportController {

    private final ClinicalReportService reportService;
    
    /**
     * Create a new clinical report
     */
    @PostMapping
    public ResponseEntity<ClinicalReport> createReport(
            @RequestParam Long patientId,
            @RequestParam Long sampleId,
            @RequestParam Long variantCallingId,
            @RequestParam String reportTitle,
            @RequestParam String reportSummary,
            @RequestParam String testMethodology,
            @RequestParam String sequencingDetails,
            @RequestParam String bioinformaticsPipeline,
            @RequestParam(required = false) String qualityMetrics,
            @RequestParam(required = false) String limitations,
            @RequestParam(required = false) String recommendations,
            @RequestParam(required = false) String additionalNotes,
            @RequestParam String createdBy) {
        
        ClinicalReport report = reportService.createReport(
                patientId, sampleId, variantCallingId, reportTitle, reportSummary,
                testMethodology, sequencingDetails, bioinformaticsPipeline,
                qualityMetrics, limitations, recommendations, additionalNotes, createdBy);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }
    
    /**
     * Generate a clinical report automatically
     */
    @PostMapping("/generate")
    public ResponseEntity<ClinicalReport> generateReport(
            @RequestParam Long patientId,
            @RequestParam Long sampleId,
            @RequestParam Long variantCallingId,
            @RequestParam String createdBy) {
        
        ClinicalReport report = reportService.generateAutomaticReport(
                patientId, sampleId, variantCallingId, createdBy);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }
    
    /**
     * Get a report by ID
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<ClinicalReport> getReportById(@PathVariable Long reportId) {
        try {
            ClinicalReport report = reportService.getReportById(reportId);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all reports for a patient
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ClinicalReport>> getReportsByPatient(@PathVariable Long patientId) {
        List<ClinicalReport> reports = reportService.getReportsByPatient(patientId);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Get the latest report for a patient
     */
    @GetMapping("/patient/{patientId}/latest")
    public ResponseEntity<ClinicalReport> getLatestReportForPatient(@PathVariable Long patientId) {
        ClinicalReport report = reportService.getLatestReportForPatient(patientId);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(report);
    }
    
    /**
     * Add interpretations to a report
     */
    @PostMapping("/{reportId}/interpretations")
    public ResponseEntity<ClinicalReport> addInterpretationsToReport(
            @PathVariable Long reportId,
            @RequestBody List<Long> interpretationIds) {
        
        try {
            ClinicalReport report = reportService.addInterpretationsToReport(reportId, interpretationIds);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Submit a report for review
     */
    @PostMapping("/{reportId}/submit")
    public ResponseEntity<ClinicalReport> submitReportForReview(@PathVariable Long reportId) {
        try {
            ClinicalReport report = reportService.submitReportForReview(reportId);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Add a comment to a report
     */
    @PostMapping("/{reportId}/comments")
    public ResponseEntity<ReportComment> addCommentToReport(
            @PathVariable Long reportId,
            @RequestParam String commentText,
            @RequestParam(required = false) String commentSection,
            @RequestParam ReportComment.CommentType commentType,
            @RequestParam String commentedBy) {
        
        try {
            ReportComment comment = reportService.addCommentToReport(
                    reportId, commentText, commentSection, commentType, commentedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all comments for a report
     */
    @GetMapping("/{reportId}/comments")
    public ResponseEntity<List<ReportComment>> getCommentsForReport(@PathVariable Long reportId) {
        List<ReportComment> comments = reportService.getCommentsForReport(reportId);
        return ResponseEntity.ok(comments);
    }
    
    /**
     * Get unresolved comments for a report
     */
    @GetMapping("/{reportId}/comments/unresolved")
    public ResponseEntity<List<ReportComment>> getUnresolvedCommentsForReport(@PathVariable Long reportId) {
        List<ReportComment> comments = reportService.getUnresolvedCommentsForReport(reportId);
        return ResponseEntity.ok(comments);
    }
    
    /**
     * Resolve a comment
     */
    @PostMapping("/comments/{commentId}/resolve")
    public ResponseEntity<ReportComment> resolveComment(
            @PathVariable Long commentId,
            @RequestParam String resolvedBy,
            @RequestParam(required = false) String resolutionNotes) {
        
        try {
            ReportComment comment = reportService.resolveComment(commentId, resolvedBy, resolutionNotes);
            return ResponseEntity.ok(comment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Approve a report
     */
    @PostMapping("/{reportId}/approve")
    public ResponseEntity<ClinicalReport> approveReport(
            @PathVariable Long reportId,
            @RequestParam String approvedBy) {
        
        try {
            ClinicalReport report = reportService.approveReport(reportId, approvedBy);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Finalize a report
     */
    @PostMapping("/{reportId}/finalize")
    public ResponseEntity<ClinicalReport> finalizeReport(
            @PathVariable Long reportId,
            @RequestParam(required = false) String pdfPath) {
        
        try {
            ClinicalReport report = reportService.finalizeReport(reportId, pdfPath);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Amend a report
     */
    @PostMapping("/{reportId}/amend")
    public ResponseEntity<ClinicalReport> amendReport(
            @PathVariable Long reportId,
            @RequestParam String createdBy) {
        
        try {
            ClinicalReport report = reportService.amendReport(reportId, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
