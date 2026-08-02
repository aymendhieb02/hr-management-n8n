package com.xtensus.hrmanagementapi.certificat.medical.service;

import com.xtensus.hrmanagementapi.domain.entity.EmployeCertificatMedical;
import org.springframework.core.io.Resource;

public record CertificatMedicalDownload(EmployeCertificatMedical certificat, Resource resource) {
}
