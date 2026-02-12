package com.skillbridge.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service") // It looks up the URL from Eureka
public interface UserServiceClient {

  @GetMapping("/api/v1/users/exists/{id}")
  boolean checkUserExists(@PathVariable("id") Long id);
}
