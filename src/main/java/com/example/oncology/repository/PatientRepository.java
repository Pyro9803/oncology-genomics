package com.example.oncology.repository;

import com.example.oncology.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    Optional<Patient> findByMedicalRecordNumber(String medicalRecordNumber);
    
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
    
    @Query("SELECT p FROM Patient p JOIN p.diagnoses d WHERE d.cancerType = :cancerType")
    List<Patient> findByCancerType(String cancerType);
    
    @Query("SELECT p FROM Patient p JOIN p.samples s JOIN s.tumorVariantCallings vc " +
           "JOIN vc.somaticVariants sv WHERE sv.geneSymbol = :geneSymbol")
    List<Patient> findByVariantInGene(String geneSymbol);
    
    // Find patients by age range
    @Query(value = "SELECT * FROM patient p WHERE EXTRACT(YEAR FROM AGE(CURRENT_DATE, p.date_of_birth)) BETWEEN :minAge AND :maxAge", nativeQuery = true)
    List<Patient> findByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
    
    // Find patients by name (case insensitive)
    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}
