package com.skillbridge.exception;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CategoryNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handle(CategoryNotFoundException exp) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiErrorResponse(404, exp.getMessage(), null));
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handle(UserNotFoundException exp) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiErrorResponse(404, exp.getMessage(), null));
  }

  @ExceptionHandler(ListingNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handle(ListingNotFoundException exp) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(404, exp.getMessage(), null));
  }

  @ExceptionHandler(ApplicationNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handle(ApplicationNotFoundException exp) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(404, exp.getMessage(), null));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exp) {
    var errors = new HashMap<String, String>();
    exp.getBindingResult().getAllErrors().forEach(error -> {
      var fieldName = ((FieldError) error).getField();
      var errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiErrorResponse(400, "Validation Failed", errors));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException exp) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiErrorResponse(400, exp.getMessage(), null));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exp) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiErrorResponse(400, exp.getMessage(), null));
  }
  // // 3. The "Safety Net" - catches everything else
  // @ExceptionHandler(Exception.class)
  // public ResponseEntity<ApiErrorResponse> handleGeneral(Exception exp) {
  // // Log the actual error for debugging, but hide details from the user
  // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
  // .body(new ApiErrorResponse(500, "An unexpected error occurred", null));
  // }

  @ExceptionHandler(java.lang.reflect.UndeclaredThrowableException.class)
  public ResponseEntity<ApiErrorResponse> handleUndeclared(java.lang.reflect.UndeclaredThrowableException exp) {
    // Get the actual cause inside the wrapper
    Throwable cause = exp.getCause();
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new ApiErrorResponse(503, cause.getMessage(), null));
  }

  // Handle the specific RuntimeException from our Feign Decoder
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException exp) {
    // We can check the message to determine if it's a Feign error
    HttpStatus status = exp.getMessage().contains("User Service")
        ? HttpStatus.SERVICE_UNAVAILABLE
        : HttpStatus.INTERNAL_SERVER_ERROR;

    return ResponseEntity.status(status)
        .body(new ApiErrorResponse(status.value(), exp.getMessage(), null));
  }

  // This handles the "Connection Refused" when service is totally down
  @ExceptionHandler(feign.RetryableException.class)
  public ResponseEntity<ApiErrorResponse> handleFeignRetryable(feign.RetryableException e) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new ApiErrorResponse(503, "User Service is offline or unreachable.", null));
  }
}