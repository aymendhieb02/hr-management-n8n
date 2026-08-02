package com.xtensus.hrmanagementapi.certificat.medical.mapper;

import com.xtensus.hrmanagementapi.certificat.medical.dto.CertificatMedicalResponse;
import com.xtensus.hrmanagementapi.domain.entity.EmployeCertificatMedical;
import org.springframework.stereotype.Component;

@Component
public class CertificatMedicalMapper {
    public CertificatMedicalResponse toResponse(EmployeCertificatMedical certificat) {
        CertificatMedicalResponse response = new CertificatMedicalResponse();
        response.setId(certificat.getId());
        response.setCongeDemandeId(certificat.getCongeDemande().getId());
        response.setNomFichier(certificat.getNomFichier());
        response.setTypeMime(certificat.getTypeMime());
        response.setTailleFichier(certificat.getTailleFichier());
        response.setDateSoumission(certificat.getDateSoumission());
        return response;
    }
}
