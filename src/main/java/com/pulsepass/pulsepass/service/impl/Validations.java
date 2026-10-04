package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.exception.BusinessRuleException;

/** Validaciones básicas de entrada compartidas por las implementaciones. */
final class Validations {

    private Validations() {}

    static <T> T requireRequest(T request) {
        if (request == null) {
            throw new BusinessRuleException("Request is required.");
        }
        return request;
    }

    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(field + " is required.");
        }
        return value.trim();
    }

    static <T> T requireValue(T value, String field) {
        if (value == null) {
            throw new BusinessRuleException(field + " is required.");
        }
        return value;
    }
}
