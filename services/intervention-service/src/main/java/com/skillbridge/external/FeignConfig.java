package com.skillbridge.external;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Retryer;
import feign.codec.ErrorDecoder;

@Configuration
public class FeignConfig {
  @Bean
  public ErrorDecoder errorDecoder() {
    return new CustomFeignErrorDecoder();
  }

  @Bean
  public Retryer feignRetryer() {
    // Period: 100ms, Max Period: 1s, Max Attempts: 3
    return new Retryer.Default(100, 1000, 3);
  }
}
