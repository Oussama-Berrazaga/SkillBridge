package com.skillbridge.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AccessDeniedException extends RuntimeException {

  private final String message;
}
