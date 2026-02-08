package com.skillbridge.exception;

import java.util.Map;

public record ApiErrorResponse(
        int status,
        String message,
        Map<String, String> errors // Only populated for validation errors
) {
}