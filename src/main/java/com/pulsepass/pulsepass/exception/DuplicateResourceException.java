package com.pulsepass.pulsepass.exception;

/** Conflicto de unicidad. Ej.: "Username already exists." */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
