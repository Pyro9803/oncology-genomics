package com.example.oncology.repository;

import com.example.oncology.entity.Diagnosis;
import com.example.oncology.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    
    List<Diagnosis> findByPatientPatientId(Long patientId);
    
    List<Diagnosis> findByCancerTypeContainingIgnoreCase(String cancerType);
    
    List<Diagnosis> findByPatient(Patient patient);
    
    List<Diagnosis> findByStageGroup(String stageGroup);
}
