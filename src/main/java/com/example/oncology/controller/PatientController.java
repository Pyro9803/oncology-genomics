package com.example.oncology.controller;

import com.example.oncology.entity.Diagnosis;
import com.example.oncology.entity.Patient;
import com.example.oncology.repository.DiagnosisRepository;
import com.example.oncology.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for patient operations
 */
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final DiagnosisRepository diagnosisRepository;
    
    /**
     * Get all patients
     */
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Get patient by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new patient
     */
    @PostMapping
    public ResponseEntity<Patient> createPatient(
            @RequestParam String medicalRecordNumber,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam String gender,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address) {
        
        Patient patient = new Patient();
        patient.setMedicalRecordNumber(medicalRecordNumber);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setContactNumber(contactNumber);
        patient.setEmail(email);
        patient.setAddress(address);
        
        Patient savedPatient = patientRepository.save(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPatient);
    }
    
    /**
     * Update an existing patient
     */
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable Long id,
            @RequestParam(required = false) String medicalRecordNumber,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address) {
        
        return patientRepository.findById(id)
                .map(patient -> {
                    if (medicalRecordNumber != null) patient.setMedicalRecordNumber(medicalRecordNumber);
                    if (firstName != null) patient.setFirstName(firstName);
                    if (lastName != null) patient.setLastName(lastName);
                    if (dateOfBirth != null) patient.setDateOfBirth(dateOfBirth);
                    if (gender != null) patient.setGender(gender);
                    if (contactNumber != null) patient.setContactNumber(contactNumber);
                    if (email != null) patient.setEmail(email);
                    if (address != null) patient.setAddress(address);
                    
                    Patient updatedPatient = patientRepository.save(patient);
                    return ResponseEntity.ok(updatedPatient);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete a patient
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(patient -> {
                    patientRepository.delete(patient);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Search patients by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<Patient>> searchPatients(@RequestParam String name) {
        List<Patient> patients = patientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Add a diagnosis to a patient
     */
    @PostMapping("/{patientId}/diagnoses")
    public ResponseEntity<Diagnosis> addDiagnosis(
            @PathVariable Long patientId,
            @RequestParam String cancerType,
            @RequestParam(required = false) String histology,
            @RequestParam(required = false) String tStage,
            @RequestParam(required = false) String nStage,
            @RequestParam(required = false) String mStage,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate diagnosisDate) {
        
        return patientRepository.findById(patientId)
                .map(patient -> {
                    Diagnosis diagnosis = new Diagnosis();
                    diagnosis.setPatient(patient);
                    diagnosis.setCancerType(cancerType);
                    diagnosis.setHistology(histology);
                    diagnosis.setTStage(tStage);
                    diagnosis.setNStage(nStage);
                    diagnosis.setMStage(mStage);
                    diagnosis.setDiagnosisDate(diagnosisDate);
                    
                    Diagnosis savedDiagnosis = diagnosisRepository.save(diagnosis);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedDiagnosis);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all diagnoses for a patient
     */
    @GetMapping("/{patientId}/diagnoses")
    public ResponseEntity<List<Diagnosis>> getPatientDiagnoses(@PathVariable Long patientId) {
        return patientRepository.findById(patientId)
                .map(patient -> {
                    List<Diagnosis> diagnoses = diagnosisRepository.findByPatient(patient);
                    return ResponseEntity.ok(diagnoses);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
