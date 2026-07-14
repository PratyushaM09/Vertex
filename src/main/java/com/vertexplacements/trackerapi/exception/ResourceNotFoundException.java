package com.vertexplacements.trackerapi.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forCompany(Long id) {
        return new ResourceNotFoundException("Company not found with id: " + id);
    }

    public static ResourceNotFoundException forApplication(Long id) {
        return new ResourceNotFoundException("Application not found with id: " + id);
    }

    public static ResourceNotFoundException forUser(String email) {
        return new ResourceNotFoundException("User not found with email: " + email);
    }
}
