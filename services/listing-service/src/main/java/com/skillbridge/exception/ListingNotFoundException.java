package com.skillbridge.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ListingNotFoundException extends RuntimeException {

    private final String message;
}