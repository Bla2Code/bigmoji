package com.bigmoji.config;

import io.minio.ListBucketsArgs;
import io.minio.MinioClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthConfig {
  @Bean
  HealthIndicator minioHealthIndicator(MinioClient minioClient) {
    return () -> {
      try {
        minioClient.listBuckets(ListBucketsArgs.builder().build());
        return Health.up().withDetail("minio", "UP").build();
      } catch (Exception e) {
        return Health.down(e).withDetail("minio", "DOWN").build();
      }
    };
  }
}
