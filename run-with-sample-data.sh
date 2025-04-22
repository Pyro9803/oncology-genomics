#!/bin/bash
# Script to run the Oncology Genomics application with sample data

echo "Starting Oncology Genomics application..."
docker-compose up -d

# Wait for the application to be ready
echo "Waiting for the application to be ready..."
until $(curl --output /dev/null --silent --head --fail http://localhost:8080/actuator/health); do
    printf '.'
    sleep 5
done
echo -e "\nApplication is ready!"

# Install required Python packages for data generation
echo "Installing required Python packages..."
pip install requests faker

# Generate sample data
echo "Generating sample data..."
python generate_sample_data.py --patients 10 --verbose

echo "Sample data generation complete!"
echo "You can now access the application at http://localhost:8080"
echo "To test the API endpoints, run: ./test-api.sh"
