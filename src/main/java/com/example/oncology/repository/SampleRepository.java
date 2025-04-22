package com.example.oncology.repository;

import com.example.oncology.entity.Diagnosis;
import com.example.oncology.entity.Patient;
import com.example.oncology.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {
    
    // Find samples by patient ID
    List<Sample> findByPatientPatientId(Long patientId);
    
    // Find samples by patient
    List<Sample> findByPatient(Patient patient);
    
    // Find samples by diagnosis
    List<Sample> findByDiagnosis(Diagnosis diagnosis);
    
    List<Sample> findBySampleType(String sampleType);
    
    List<Sample> findByDiagnosisDiagnosisId(Long diagnosisId);
    
    @Query("SELECT s FROM Sample s WHERE s.tumorPurity >= :minPurity")
    List<Sample> findByMinimumTumorPurity(Double minPurity);
}
