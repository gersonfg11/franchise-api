package com.accenture.franchiseapi.infrastructure.exception;

public class BranchNotFoundException extends RuntimeException {
    public BranchNotFoundException(String id) {
        super("Branch not found with id: " + id);
    }
}
