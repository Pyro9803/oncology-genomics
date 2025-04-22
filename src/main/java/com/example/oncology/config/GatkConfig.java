package com.example.oncology.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for GATK integration
 */
@Configuration
@EnableAsync
public class GatkConfig {

    /**
     * Configure async executor for GATK jobs
     */
    @Bean(name = "gatkTaskExecutor")
    public Executor gatkTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("gatk-");
        executor.initialize();
        return executor;
    }
    
    /**
     * Configure ObjectMapper for JSON processing with Java 8 date/time support
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Register JavaTimeModule for handling Java 8 date/time types (LocalDate, LocalDateTime, etc.)
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        // Configure to not write dates as timestamps (use ISO-8601 format instead)
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
