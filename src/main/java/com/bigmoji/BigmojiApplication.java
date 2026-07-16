package com.bigmoji;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BigmojiApplication {
  public static void main(String[] args) {
    SpringApplication.run(BigmojiApplication.class, args);
  }
}
