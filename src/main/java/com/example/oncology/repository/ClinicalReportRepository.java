package com.example.oncology.repository;

import com.example.oncology.entity.ClinicalReport;
import com.example.oncology.entity.ClinicalReport.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for clinical report operations
 */
@Repository
public interface ClinicalReportRepository extends JpaRepository<ClinicalReport, Long> {
    
    List<ClinicalReport> findByPatientPatientId(Long patientId);
    
    List<ClinicalReport> findBySampleSampleId(Long sampleId);
    
    List<ClinicalReport> findByVariantCallingVariantCallingId(Long variantCallingId);
    
    List<ClinicalReport> findByReportStatus(ReportStatus status);
    
    @Query("SELECT cr FROM ClinicalReport cr WHERE cr.patient.patientId = :patientId ORDER BY cr.createdAt DESC")
    List<ClinicalReport> findLatestReportsByPatient(@Param("patientId") Long patientId);
    
    @Query("SELECT cr FROM ClinicalReport cr JOIN cr.clinicalInterpretations ci " +
           "WHERE ci.interpretationId = :interpretationId")
    List<ClinicalReport> findByInterpretationId(@Param("interpretationId") Long interpretationId);
}
