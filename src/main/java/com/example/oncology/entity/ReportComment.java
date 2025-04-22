package com.example.oncology.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing comments on clinical reports during the review process
 */
@Data
@Entity
@Table(name = "report_comment")
public class ReportComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private ClinicalReport clinicalReport;
    
    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;
    
    @Column(name = "comment_section")
    private String commentSection;
    
    @Column(name = "comment_type")
    @Enumerated(EnumType.STRING)
    private CommentType commentType;
    
    @Column(name = "commented_by", nullable = false)
    private String commentedBy;
    
    @Column(name = "resolved")
    private Boolean resolved;
    
    @Column(name = "resolved_by")
    private String resolvedBy;
    
    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Type of comment on a clinical report
     */
    public enum CommentType {
        CORRECTION,
        SUGGESTION,
        QUESTION,
        APPROVAL
    }
}
