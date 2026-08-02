package com.xtensus.hrmanagementapi.certificat.medical.exception;

public class CertificatMedicalExisteDejaException extends RuntimeException {
    public CertificatMedicalExisteDejaException(Long congeDemandeId) {
        super("Un certificat medical existe deja pour la demande de conge " + congeDemandeId);
    }
}
