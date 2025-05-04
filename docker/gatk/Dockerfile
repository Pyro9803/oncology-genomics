FROM broadinstitute/gatk:4.4.0.0

# Install additional dependencies
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    curl \
    wget \
    unzip \
    git \
    build-essential \
    zlib1g-dev \
    libbz2-dev \
    liblzma-dev \
    libcurl4-openssl-dev \
    libssl-dev \
    jq \
    && rm -rf /var/lib/apt/lists/*

# Install Python packages for variant processing
RUN pip3 install --no-cache-dir \
    pandas \
    numpy \
    scipy \
    pyvcf \
    biopython \
    && pip3 install --no-cache-dir pysam==0.19.0

# Create directories for data
RUN mkdir -p /data/references /data/inputs /data/outputs

# Set working directory
WORKDIR /data

# Add scripts for variant calling
COPY scripts /scripts
RUN chmod +x /scripts/*.sh

# Set entrypoint
ENTRYPOINT ["/bin/bash"]
CMD ["/scripts/run_pipeline.sh"]
