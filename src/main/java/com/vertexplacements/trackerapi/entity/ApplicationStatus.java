package com.vertexplacements.trackerapi.entity;

/**
 * Mirrors the status pipeline used in the frontend status-dropdown:
 * Applied -> Shortlisted -> Selected / Rejected
 */
public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    SELECTED,
    REJECTED
}
