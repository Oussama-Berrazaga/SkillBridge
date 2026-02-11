package com.skillbridge.external;

import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.BadRequestException;

public class CustomFeignErrorDecoder implements ErrorDecoder {
  @Override
  public Exception decode(String methodKey, Response response) {
    return switch (response.status()) {
      case 400 -> new BadRequestException("External request was malformed");
      case 404 -> new EntityNotFoundException("The requested user was not found in the remote service");
      // Use a specific RuntimeException instead of the generic Exception
      default -> new RuntimeException("User Service is currently unavailable (Status: " + response.status() + ")");
    };
  }
}