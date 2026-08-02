package com.xtensus.hrmanagementapi.certificat.medical.exception;

public class CertificatMedicalIntrouvableException extends RuntimeException {
    public CertificatMedicalIntrouvableException(Long id) {
        super("Certificat medical introuvable avec l'identifiant " + id);
    }
}
