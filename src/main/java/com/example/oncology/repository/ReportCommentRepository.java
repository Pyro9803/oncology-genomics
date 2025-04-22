package com.example.oncology.repository;

import com.example.oncology.entity.ReportComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for report comment operations
 */
@Repository
public interface ReportCommentRepository extends JpaRepository<ReportComment, Long> {
    
    List<ReportComment> findByClinicalReportReportId(Long reportId);
    
    List<ReportComment> findByClinicalReportReportIdAndResolved(Long reportId, Boolean resolved);
    
    @Query("SELECT rc FROM ReportComment rc WHERE rc.clinicalReport.reportId = :reportId " +
           "ORDER BY rc.createdAt DESC")
    List<ReportComment> findLatestCommentsByReport(@Param("reportId") Long reportId);
}
