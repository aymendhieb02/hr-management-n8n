package com.xtensus.hrmanagementapi.medical.document.exception;

public class MedicalDocumentNotFoundException extends RuntimeException {

    public MedicalDocumentNotFoundException(Long id) {
        super("Medical document not found with id: " + id);
    }
}
