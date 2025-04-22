#!/usr/bin/env python3
"""
Sample Data Generator for Oncology Genomics Pipeline

This script generates realistic sample data for the oncology genomics pipeline
and populates the database with it.
"""

import requests
import json
import random
import time
from datetime import datetime, timedelta
from faker import Faker
import argparse

# Initialize Faker
fake = Faker()

# Configuration
BASE_URL = "http://localhost:8080"
TIMEOUT = 10  # seconds

# Cancer types with appropriate staging
CANCER_TYPES = [
    {
        "name": "Non-Small Cell Lung Cancer",
        "histologies": ["Adenocarcinoma", "Squamous Cell Carcinoma", "Large Cell Carcinoma"],
        "common_mutations": ["EGFR", "ALK", "ROS1", "KRAS", "BRAF", "MET", "RET", "NTRK"]
    },
    {
        "name": "Breast Cancer",
        "histologies": ["Invasive Ductal Carcinoma", "Invasive Lobular Carcinoma", "Triple Negative"],
        "common_mutations": ["BRCA1", "BRCA2", "PIK3CA", "TP53", "PTEN", "CDH1", "PALB2"]
    },
    {
        "name": "Colorectal Cancer",
        "histologies": ["Adenocarcinoma", "Mucinous Adenocarcinoma"],
        "common_mutations": ["APC", "KRAS", "BRAF", "PIK3CA", "TP53", "SMAD4", "PTEN"]
    },
    {
        "name": "Melanoma",
        "histologies": ["Superficial Spreading", "Nodular", "Lentigo Maligna", "Acral Lentiginous"],
        "common_mutations": ["BRAF", "NRAS", "NF1", "KIT", "PTEN", "CDKN2A"]
    },
    {
        "name": "Ovarian Cancer",
        "histologies": ["High-Grade Serous", "Low-Grade Serous", "Clear Cell", "Endometrioid"],
        "common_mutations": ["BRCA1", "BRCA2", "TP53", "PTEN", "PIK3CA", "ARID1A"]
    }
]

# T Stages
T_STAGES = ["T1a", "T1b", "T1c", "T2a", "T2b", "T3", "T4a", "T4b"]

# N Stages
N_STAGES = ["N0", "N1", "N2a", "N2b", "N3"]

# M Stages
M_STAGES = ["M0", "M1a", "M1b", "M1c"]

# Sample types
SAMPLE_TYPES = ["Tumor Tissue", "Blood", "Bone Marrow", "Pleural Fluid", "Cerebrospinal Fluid"]

# Sequencing platforms
SEQUENCING_PLATFORMS = [
    "Illumina NovaSeq 6000",
    "Illumina NextSeq 550",
    "Illumina MiSeq",
    "Ion Torrent S5",
    "PacBio Sequel II",
    "Oxford Nanopore PromethION"
]

# Library preparation methods
LIBRARY_PREPS = [
    "TruSeq DNA PCR-Free",
    "Nextera DNA Flex",
    "SureSelect Human All Exon V7",
    "Agilent SureSelect XT",
    "IDT xGen Exome Research Panel",
    "Swift Accel-NGS 2S Plus"
]

# Clinical statuses
CLINICAL_STATUSES = [
    "Complete Response",
    "Partial Response",
    "Stable Disease",
    "Progressive Disease",
    "Not Evaluable"
]

# Drugs by cancer type
DRUGS = {
    "Non-Small Cell Lung Cancer": [
        {"name": "Osimertinib", "dosage": "80mg daily", "gene": "EGFR"},
        {"name": "Alectinib", "dosage": "600mg twice daily", "gene": "ALK"},
        {"name": "Entrectinib", "dosage": "600mg daily", "gene": "ROS1"},
        {"name": "Sotorasib", "dosage": "960mg daily", "gene": "KRAS"},
        {"name": "Dabrafenib + Trametinib", "dosage": "150mg twice daily + 2mg daily", "gene": "BRAF"}
    ],
    "Breast Cancer": [
        {"name": "Olaparib", "dosage": "300mg twice daily", "gene": "BRCA1/2"},
        {"name": "Alpelisib + Fulvestrant", "dosage": "300mg daily + 500mg monthly", "gene": "PIK3CA"},
        {"name": "Trastuzumab", "dosage": "6mg/kg every 3 weeks", "gene": "HER2"},
        {"name": "Ribociclib + Letrozole", "dosage": "600mg daily + 2.5mg daily", "gene": "ESR1"}
    ],
    "Colorectal Cancer": [
        {"name": "Cetuximab", "dosage": "500mg/m² every 2 weeks", "gene": "KRAS-wildtype"},
        {"name": "Encorafenib + Cetuximab", "dosage": "300mg daily + 500mg/m² every 2 weeks", "gene": "BRAF"},
        {"name": "Pembrolizumab", "dosage": "200mg every 3 weeks", "gene": "MSI-H/dMMR"}
    ],
    "Melanoma": [
        {"name": "Dabrafenib + Trametinib", "dosage": "150mg twice daily + 2mg daily", "gene": "BRAF"},
        {"name": "Pembrolizumab", "dosage": "200mg every 3 weeks", "gene": "PD-L1"},
        {"name": "Nivolumab + Ipilimumab", "dosage": "3mg/kg + 1mg/kg every 3 weeks", "gene": "PD-L1/CTLA-4"}
    ],
    "Ovarian Cancer": [
        {"name": "Olaparib", "dosage": "300mg twice daily", "gene": "BRCA1/2"},
        {"name": "Niraparib", "dosage": "200-300mg daily", "gene": "HRD-positive"},
        {"name": "Bevacizumab", "dosage": "15mg/kg every 3 weeks", "gene": "VEGF"}
    ]
}

# Evidence levels
EVIDENCE_LEVELS = ["Level 1A", "Level 1B", "Level 2A", "Level 2B", "Level 3A", "Level 3B", "Level 4"]

# Adverse events
ADVERSE_EVENTS = [
    "Grade 1 fatigue",
    "Grade 2 nausea",
    "Grade 1 rash",
    "Grade 2 diarrhea",
    "Grade 3 neutropenia",
    "Grade 1 peripheral neuropathy",
    "Grade 2 anemia",
    "Grade 1 elevated liver enzymes",
    "Grade 2 thrombocytopenia",
    "Grade 1 hypothyroidism"
]

def generate_random_date(start_date, end_date):
    """Generate a random date between start_date and end_date"""
    time_between_dates = end_date - start_date
    days_between_dates = time_between_dates.days
    random_number_of_days = random.randrange(days_between_dates)
    return start_date + timedelta(days=random_number_of_days)

def call_api(method, endpoint, data=None, description="API Call"):
    """Make an API call and return the response"""
    url = f"{BASE_URL}{endpoint}"
    headers = {"Content-Type": "application/json"}
    
    print(f"Calling: {method} {url}")
    if data:
        print(f"Data: {json.dumps(data, indent=2)}")
    
    try:
        if method.upper() == "GET":
            response = requests.get(url, timeout=TIMEOUT)
        elif method.upper() == "POST":
            response = requests.post(url, json=data, headers=headers, timeout=TIMEOUT)
        elif method.upper() == "PUT":
            response = requests.put(url, json=data, headers=headers, timeout=TIMEOUT)
        elif method.upper() == "DELETE":
            response = requests.delete(url, timeout=TIMEOUT)
        else:
            print(f"Unsupported method: {method}")
            return None
        
        response.raise_for_status()
        
        if response.status_code == 204:  # No content
            print("Response: No content (204)")
            return None
        
        response_data = response.json()
        print(f"Response: Success")
        return response_data
    
    except requests.exceptions.RequestException as e:
        print(f"Error: {str(e)}")
        return None

def generate_patients(count):
    """Generate and create patients"""
    print(f"Generating {count} patients...")
    patients = []
    
    for _ in range(count):
        gender = random.choice(["Male", "Female"])
        first_name = fake.first_name_male() if gender == "Male" else fake.first_name_female()
        last_name = fake.last_name()
        
        # Generate date of birth (between 30 and 80 years ago)
        now = datetime.now()
        start_date = now - timedelta(days=80*365)
        end_date = now - timedelta(days=30*365)
        dob = generate_random_date(start_date, end_date)
        
        patient_data = {
            "medicalRecordNumber": f"MRN{fake.unique.random_number(digits=6)}",
            "firstName": first_name,
            "lastName": last_name,
            "dateOfBirth": dob.strftime("%Y-%m-%d"),
            "gender": gender,
            "contactNumber": fake.phone_number(),
            "email": fake.email(),
            "address": fake.address().replace('\n', ', ')
        }
        
        response = call_api("POST", "/patients", patient_data, "Create patient")
        if response:
            patients.append(response)
    
    return patients

def generate_diagnoses(patients):
    """Generate and create diagnoses for patients"""
    print("Generating diagnoses...")
    diagnoses = []
    
    for patient in patients:
        # Each patient can have 1-2 diagnoses
        num_diagnoses = random.randint(1, 2)
        
        for _ in range(num_diagnoses):
            cancer_type = random.choice(CANCER_TYPES)
            
            # Generate diagnosis date (within last 2 years)
            now = datetime.now()
            start_date = now - timedelta(days=2*365)
            diagnosis_date = generate_random_date(start_date, now)
            
            diagnosis_data = {
                "cancerType": cancer_type["name"],
                "diagnosisDate": diagnosis_date.strftime("%Y-%m-%d"),
                "tStage": random.choice(T_STAGES),
                "nStage": random.choice(N_STAGES),
                "mStage": random.choice(M_STAGES),
                "histology": random.choice(cancer_type["histologies"]),
                "notes": fake.paragraph(nb_sentences=3)
            }
            
            response = call_api("POST", f"/patients/{patient['patientId']}/diagnoses", 
                              diagnosis_data, f"Add diagnosis to patient {patient['patientId']}")
            if response:
                response["cancerType"] = cancer_type
                diagnoses.append(response)
    
    return diagnoses

def generate_samples(diagnoses):
    """Generate and create samples for diagnoses"""
    print("Generating samples...")
    samples = []
    
    for diagnosis in diagnoses:
        # Each diagnosis can have 1-3 samples
        num_samples = random.randint(1, 3)
        
        for i in range(num_samples):
            # First sample is always tumor, second is always blood (normal)
            if i == 0:
                sample_type = "Tumor Tissue"
                tumor_purity = round(random.uniform(0.5, 0.95), 2)
            elif i == 1:
                sample_type = "Blood"
                tumor_purity = 0.0
            else:
                sample_type = random.choice(SAMPLE_TYPES)
                tumor_purity = round(random.uniform(0.0, 0.95), 2) if sample_type != "Blood" else 0.0
            
            # Generate collection date (after diagnosis date)
            diagnosis_date = datetime.strptime(diagnosis["diagnosisDate"], "%Y-%m-%d")
            now = datetime.now()
            collection_date = generate_random_date(diagnosis_date, now)
            
            sample_data = {
                "sampleType": sample_type,
                "collectionDate": collection_date.strftime("%Y-%m-%d"),
                "tumorPurity": tumor_purity,
                "sampleQualityScore": round(random.uniform(5.0, 10.0), 1),
                "storageLocation": f"Freezer {random.choice(['A', 'B', 'C'])}, Shelf {random.randint(1, 5)}, Box {random.randint(1, 20)}"
            }
            
            response = call_api("POST", f"/diagnoses/{diagnosis['diagnosisId']}/samples", 
                              sample_data, f"Add sample to diagnosis {diagnosis['diagnosisId']}")
            if response:
                response["sampleType"] = sample_type
                response["diagnosis"] = diagnosis
                samples.append(response)
    
    return samples

def generate_sequencing_data(samples):
    """Generate and create sequencing data for samples"""
    print("Generating sequencing data...")
    sequencing_data = []
    
    for sample in samples:
        # Generate sequencing date (after sample collection date)
        collection_date = datetime.strptime(sample["collectionDate"], "%Y-%m-%d")
        now = datetime.now()
        sequencing_date = generate_random_date(collection_date, now)
        
        # Coverage depends on sample type
        coverage = random.randint(80, 150) if sample["sampleType"] == "Tumor Tissue" else random.randint(30, 60)
        
        seq_data = {
            "platform": random.choice(SEQUENCING_PLATFORMS),
            "libraryPrep": random.choice(LIBRARY_PREPS),
            "sequencingDate": sequencing_date.strftime("%Y-%m-%d"),
            "readLength": random.choice([75, 100, 150, 250]),
            "coverage": coverage,
            "fastqPath": f"/data/fastq/sample{sample['sampleId']}_{sample['sampleType'].lower().replace(' ', '_')}"
        }
        
        response = call_api("POST", f"/samples/{sample['sampleId']}/sequencing", 
                          seq_data, f"Add sequencing data to sample {sample['sampleId']}")
        if response:
            response["sample"] = sample
            sequencing_data.append(response)
    
    return sequencing_data

def generate_gatk_jobs(samples):
    """Generate and submit GATK jobs for tumor-normal pairs"""
    print("Generating GATK jobs...")
    gatk_jobs = []
    
    # Group samples by diagnosis
    samples_by_diagnosis = {}
    for sample in samples:
        diagnosis_id = sample["diagnosis"]["diagnosisId"]
        if diagnosis_id not in samples_by_diagnosis:
            samples_by_diagnosis[diagnosis_id] = []
        samples_by_diagnosis[diagnosis_id].append(sample)
    
    # For each diagnosis with at least one tumor and one normal sample, create a GATK job
    for diagnosis_id, diagnosis_samples in samples_by_diagnosis.items():
        tumor_samples = [s for s in diagnosis_samples if s["sampleType"] == "Tumor Tissue"]
        normal_samples = [s for s in diagnosis_samples if s["sampleType"] == "Blood"]
        
        if tumor_samples and normal_samples:
            for tumor_sample in tumor_samples:
                normal_sample = normal_samples[0]  # Use the first normal sample
                
                gatk_job_data = {
                    "tumorSampleId": tumor_sample["sampleId"],
                    "normalSampleId": normal_sample["sampleId"],
                    "analysisType": "Somatic",
                    "parameters": {
                        "caller": random.choice(["Mutect2", "Strelka2", "Varscan2"]),
                        "reference": "hg38",
                        "filterSettings": random.choice(["default", "strict", "lenient"])
                    }
                }
                
                response = call_api("POST", "/gatk/submit", gatk_job_data, 
                                  f"Submit GATK job for tumor-normal pair")
                if response:
                    response["tumorSample"] = tumor_sample
                    response["normalSample"] = normal_sample
                    gatk_jobs.append(response)
    
    return gatk_jobs

def generate_variants(diagnoses):
    """Generate mock variants for testing"""
    print("Generating mock variants...")
    variants = []
    
    for diagnosis in diagnoses:
        # Generate 2-5 variants per diagnosis
        num_variants = random.randint(2, 5)
        cancer_type = diagnosis["cancerType"]
        
        for _ in range(num_variants):
            # Pick a random gene from the common mutations for this cancer type
            gene = random.choice(cancer_type["common_mutations"])
            
            # Generate mock variant data
            chromosome = random.choice(["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", 
                                        "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"])
            position = random.randint(1000000, 100000000)
            ref = random.choice(["A", "C", "G", "T"])
            alt = random.choice([b for b in ["A", "C", "G", "T"] if b != ref])
            
            # For simplicity, we'll directly create variants in the database
            # In a real scenario, these would come from VCF files processed by GATK
            
            # This is a mock call - in reality, variants would be created by the GATK pipeline
            # We're simulating this for testing purposes
            variant_data = {
                "geneSymbol": gene,
                "chromosome": chromosome,
                "position": position,
                "referenceAllele": ref,
                "alternateAllele": alt,
                "variantType": random.choice(["SNV", "Insertion", "Deletion", "CNV"]),
                "alleleFrequency": round(random.uniform(0.05, 0.5), 3),
                "readDepth": random.randint(50, 500),
                "filterStatus": random.choice(["PASS", "LowQual", "PASS", "PASS"]),  # Bias toward PASS
                "variantCallingId": 1  # Mock ID
            }
            
            # This is a mock API call - in reality, variants would be created by the GATK pipeline
            # For testing purposes, we'll assume the endpoint exists
            response = call_api("POST", "/variants", variant_data, 
                              f"Create mock variant for gene {gene}")
            if response:
                response["diagnosis"] = diagnosis
                variants.append(response)
    
    return variants

def generate_clinical_interpretations(variants):
    """Generate clinical interpretations for variants"""
    print("Generating clinical interpretations...")
    interpretations = []
    
    for variant in variants:
        gene = variant["geneSymbol"]
        diagnosis = variant["diagnosis"]
        cancer_type = diagnosis["cancerType"]["name"]
        
        # Generate interpretation based on gene and cancer type
        significance = random.choice(["Pathogenic", "Likely Pathogenic", "Variant of Unknown Significance", 
                                     "Likely Benign", "Benign"])
        
        # More likely to be pathogenic for common cancer genes
        if gene in diagnosis["cancerType"]["common_mutations"]:
            significance = random.choice(["Pathogenic", "Likely Pathogenic", "Pathogenic"])
        
        interpretation_text = f"{gene} variant in {cancer_type}. "
        
        if significance in ["Pathogenic", "Likely Pathogenic"]:
            interpretation_text += random.choice([
                f"Associated with increased tumor growth and proliferation.",
                f"May confer resistance to standard therapies.",
                f"Associated with response to targeted therapies.",
                f"Indicates potential sensitivity to immunotherapy.",
                f"Associated with poor prognosis."
            ])
        else:
            interpretation_text += random.choice([
                f"Clinical significance is currently uncertain.",
                f"Insufficient evidence to determine clinical impact.",
                f"Likely represents a benign polymorphism.",
                f"Not previously reported in this cancer type."
            ])
        
        interpretation_data = {
            "variantId": variant.get("variantId", 1),  # Mock ID if not available
            "clinicalSignificance": significance,
            "evidenceLevel": random.choice(EVIDENCE_LEVELS),
            "interpretation": interpretation_text,
            "references": f"PMID:{random.randint(10000000, 30000000)}, PMID:{random.randint(10000000, 30000000)}"
        }
        
        response = call_api("POST", "/interpretations", interpretation_data, 
                          f"Add clinical interpretation for variant in {gene}")
        if response:
            response["variant"] = variant
            interpretations.append(response)
    
    return interpretations

def generate_therapy_recommendations(patients, diagnoses, variants):
    """Generate therapy recommendations based on variants"""
    print("Generating therapy recommendations...")
    recommendations = []
    
    # Group variants by patient and diagnosis
    variants_by_patient_diagnosis = {}
    for variant in variants:
        patient_id = variant["diagnosis"]["patientId"]
        diagnosis_id = variant["diagnosis"]["diagnosisId"]
        key = f"{patient_id}_{diagnosis_id}"
        
        if key not in variants_by_patient_diagnosis:
            variants_by_patient_diagnosis[key] = []
        
        variants_by_patient_diagnosis[key].append(variant)
    
    # Generate recommendations for each patient-diagnosis pair with variants
    for key, patient_variants in variants_by_patient_diagnosis.items():
        patient_id, diagnosis_id = map(int, key.split("_"))
        
        # Find the diagnosis object
        diagnosis = next((d for d in diagnoses if d["diagnosisId"] == diagnosis_id), None)
        if not diagnosis:
            continue
        
        cancer_type = diagnosis["cancerType"]["name"]
        
        # Get potential drugs for this cancer type
        potential_drugs = DRUGS.get(cancer_type, [])
        if not potential_drugs:
            continue
        
        # Find matching drugs based on variants
        matching_drugs = []
        for variant in patient_variants:
            gene = variant["geneSymbol"]
            for drug in potential_drugs:
                if gene in drug["gene"]:
                    matching_drugs.append(drug)
        
        # If no matching drugs, pick a random one
        if not matching_drugs and potential_drugs:
            matching_drugs = [random.choice(potential_drugs)]
        
        # Create recommendation for each matching drug
        for drug in matching_drugs:
            # Generate recommendation date (after diagnosis date)
            diagnosis_date = datetime.strptime(diagnosis["diagnosisDate"], "%Y-%m-%d")
            now = datetime.now()
            recommendation_date = generate_random_date(diagnosis_date, now)
            
            recommendation_data = {
                "patientId": patient_id,
                "diagnosisId": diagnosis_id,
                "drugName": drug["name"],
                "dosage": drug["dosage"],
                "recommendationDate": recommendation_date.strftime("%Y-%m-%d"),
                "evidenceLevel": random.choice(EVIDENCE_LEVELS),
                "variantIds": [v.get("variantId", 1) for v in patient_variants if v["geneSymbol"] in drug["gene"]],
                "notes": f"Recommended based on {drug['gene']} mutation status. " + fake.paragraph(nb_sentences=2)
            }
            
            response = call_api("POST", "/therapies", recommendation_data, 
                              f"Add therapy recommendation for patient {patient_id}")
            if response:
                response["patient_id"] = patient_id
                response["diagnosis"] = diagnosis
                recommendations.append(response)
    
    return recommendations

def generate_followups(patients, recommendations):
    """Generate follow-up records for patients with therapy recommendations"""
    print("Generating follow-up records...")
    followups = []
    
    # Group recommendations by patient
    recommendations_by_patient = {}
    for rec in recommendations:
        patient_id = rec["patient_id"]
        if patient_id not in recommendations_by_patient:
            recommendations_by_patient[patient_id] = []
        recommendations_by_patient[patient_id].append(rec)
    
    # Generate follow-ups for each patient with recommendations
    for patient_id, patient_recommendations in recommendations_by_patient.items():
        # Generate 1-3 follow-ups per patient
        num_followups = random.randint(1, 3)
        
        # Sort recommendations by date
        sorted_recommendations = sorted(patient_recommendations, 
                                       key=lambda x: datetime.strptime(x["recommendationDate"], "%Y-%m-%d"))
        
        # Get the earliest recommendation date
        earliest_rec_date = datetime.strptime(sorted_recommendations[0]["recommendationDate"], "%Y-%m-%d")
        
        # Generate follow-up dates (after earliest recommendation date)
        followup_dates = []
        current_date = earliest_rec_date + timedelta(days=random.randint(30, 60))
        
        for _ in range(num_followups):
            if current_date < datetime.now():
                followup_dates.append(current_date)
                current_date += timedelta(days=random.randint(60, 120))
            else:
                break
        
        # Create follow-up records
        for i, followup_date in enumerate(followup_dates):
            # Clinical status tends to improve and then potentially worsen
            if i == 0:
                status = random.choice(["Partial Response", "Stable Disease"])
            elif i == len(followup_dates) - 1:
                status = random.choice(["Partial Response", "Stable Disease", "Progressive Disease"])
            else:
                status = random.choice(["Partial Response", "Complete Response", "Stable Disease"])
            
            # Generate imaging results based on status
            if status == "Complete Response":
                imaging = "No evidence of disease on imaging."
            elif status == "Partial Response":
                imaging = f"{random.randint(30, 70)}% reduction in tumor size compared to baseline."
            elif status == "Stable Disease":
                imaging = "No significant change in tumor measurements since last assessment."
            elif status == "Progressive Disease":
                imaging = f"{random.randint(20, 50)}% increase in tumor size. New lesions detected in {random.choice(['liver', 'lung', 'bone', 'brain'])}."
            else:
                imaging = "Imaging studies inconclusive."
            
            # Generate adverse events (0-3)
            num_adverse_events = random.randint(0, 3)
            adverse_events_list = random.sample(ADVERSE_EVENTS, num_adverse_events)
            adverse_events = ", ".join(adverse_events_list) if adverse_events_list else "None"
            
            # Next follow-up date
            next_followup_date = followup_date + timedelta(days=random.randint(60, 120))
            
            followup_data = {
                "patientId": patient_id,
                "followUpDate": followup_date.strftime("%Y-%m-%d"),
                "clinicalStatus": status,
                "imagingResults": imaging,
                "adverseEvents": adverse_events,
                "nextFollowUpDate": next_followup_date.strftime("%Y-%m-%d"),
                "notes": fake.paragraph(nb_sentences=3)
            }
            
            response = call_api("POST", "/followups", followup_data, 
                              f"Add follow-up record for patient {patient_id}")
            if response:
                followups.append(response)
    
    return followups

def wait_for_application():
    """Wait for the application to be ready"""
    print("Waiting for the application to be ready...")
    max_retries = 30
    
    for i in range(max_retries):
        try:
            response = requests.get(f"{BASE_URL}/actuator/health", timeout=TIMEOUT)
            if response.status_code == 200:
                print("Application is ready!")
                return True
        except requests.exceptions.RequestException:
            pass
        
        print(".", end="", flush=True)
        time.sleep(5)
    
    print("\nApplication did not become ready in time.")
    return False

def main():
    """Main function to generate sample data"""
    parser = argparse.ArgumentParser(description="Generate sample data for Oncology Genomics Pipeline")
    parser.add_argument("--patients", type=int, default=5, help="Number of patients to generate")
    args = parser.parse_args()
    
    print("ONCOLOGY GENOMICS PIPELINE SAMPLE DATA GENERATOR")
    print(f"Base URL: {BASE_URL}")
    print(f"Timestamp: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    if not wait_for_application():
        return
    
    # Generate data
    patients = generate_patients(args.patients)
    diagnoses = generate_diagnoses(patients)
    samples = generate_samples(diagnoses)
    sequencing_data = generate_sequencing_data(samples)
    gatk_jobs = generate_gatk_jobs(samples)
    variants = generate_variants(diagnoses)
    interpretations = generate_clinical_interpretations(variants)
    recommendations = generate_therapy_recommendations(patients, diagnoses, variants)
    followups = generate_followups(patients, recommendations)
    
    # Print summary
    print("\nDATA GENERATION SUMMARY")
    print(f"Generated {len(patients)} patients")
    print(f"Generated {len(diagnoses)} diagnoses")
    print(f"Generated {len(samples)} samples")
    print(f"Generated {len(sequencing_data)} sequencing data records")
    print(f"Generated {len(gatk_jobs)} GATK jobs")
    print(f"Generated {len(variants)} variants")
    print(f"Generated {len(interpretations)} clinical interpretations")
    print(f"Generated {len(recommendations)} therapy recommendations")
    print(f"Generated {len(followups)} follow-up records")
    
    print("\nSample data generation complete!")

if __name__ == "__main__":
    main()
