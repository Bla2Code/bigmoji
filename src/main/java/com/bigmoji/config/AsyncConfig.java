package com.bigmoji.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
  @Bean(name = "messageExecutor")
  ThreadPoolTaskExecutor messageExecutor(
      @Value("${bigmoji.async.core-pool-size:10}") int core,
      @Value("${bigmoji.async.max-pool-size:20}") int max) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(core);
    executor.setMaxPoolSize(max);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("bigmoji-msg-");
    executor.initialize();
    return executor;
  }
}
