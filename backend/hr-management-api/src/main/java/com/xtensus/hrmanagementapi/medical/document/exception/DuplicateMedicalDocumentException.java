package com.xtensus.hrmanagementapi.medical.document.exception;

public class DuplicateMedicalDocumentException extends RuntimeException {

    public DuplicateMedicalDocumentException(Long leaveRequestId) {
        super("Medical document already exists for leave request: " + leaveRequestId);
    }
}
