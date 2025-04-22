# Oncology Genomics Database Application

This Spring Boot application provides a comprehensive database model for somatic variant analysis in cancer diagnostics using GATK. It supports clinical applications and patient management with a focus on variant interpretation and therapy recommendations.

## Features

- **Patient Management**: Track patient information, diagnoses, and follow-ups
- **Sample Tracking**: Manage tumor and normal samples with metadata
- **GATK Integration**: Submit and monitor variant calling jobs
- **Variant Analysis**: Store and analyze somatic variants
- **Clinical Interpretation**: Interpret variants for clinical significance
- **Therapy Recommendations**: Generate and track therapy recommendations
- **Follow-up Management**: Record and analyze patient outcomes

## Technology Stack

- **Backend**: Spring Boot 3.x, Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Containerization**: Docker, Docker Compose
- **Variant Calling**: GATK 4.4.0

## Project Structure

```
oncology/
├── src/
│   ├── main/
│   │   ├── java/com/example/oncology/
│   │   │   ├── controller/    # REST API controllers
│   │   │   ├── entity/        # JPA entity classes
│   │   │   ├── repository/    # Spring Data repositories
│   │   │   ├── service/       # Business logic services
│   │   │   └── config/        # Application configuration
│   │   └── resources/
│   │       ├── application.properties  # Application configuration
│   │       └── schema.sql              # Database schema
├── docker/
│   └── gatk/                  # GATK Docker configuration
│       ├── Dockerfile
│       └── scripts/           # Pipeline scripts
├── data/                      # Data directories
│   ├── inputs/                # Input files for GATK
│   ├── outputs/               # Output files from GATK
│   └── references/            # Reference genomes
├── docker-compose.yml         # Docker Compose configuration
├── Dockerfile                 # Application Dockerfile
└── pom.xml                    # Maven dependencies
```

## Setup and Installation

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Maven

### Database Setup

The application uses PostgreSQL for data storage. The database will be automatically created when running with Docker Compose.

### Running the Application

1. Clone the repository
2. Build the application:
   ```
   mvn clean package
   ```
3. Start the application with Docker Compose:
   ```
   docker-compose up -d
   ```

### Accessing the Application

The application will be available at http://localhost:8080/oncology

## API Documentation

The application provides RESTful APIs for:

- Patient management
- Sample management
- GATK analysis submission and monitoring
- Variant analysis
- Clinical interpretation
- Therapy recommendations
- Follow-up management

## Development

### Adding New Features

1. Create entity classes in the `entity` package
2. Create repository interfaces in the `repository` package
3. Implement service classes in the `service` package
4. Create controllers in the `controller` package

### Testing

Run tests with Maven:
```
mvn test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
