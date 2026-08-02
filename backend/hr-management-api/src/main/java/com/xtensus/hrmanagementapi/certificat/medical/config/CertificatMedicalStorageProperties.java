package com.xtensus.hrmanagementapi.certificat.medical.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Component
@ConfigurationProperties(prefix = "app.storage")
public class CertificatMedicalStorageProperties {
    private String medicalCertificatesPath;
    private DataSize maxFileSize = DataSize.ofMegabytes(5);
    private List<String> allowedTypes = List.of("application/pdf", "image/jpeg", "image/png");

    public String getMedicalCertificatesPath() { return medicalCertificatesPath; }
    public void setMedicalCertificatesPath(String medicalCertificatesPath) { this.medicalCertificatesPath = medicalCertificatesPath; }
    public DataSize getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(DataSize maxFileSize) { this.maxFileSize = maxFileSize; }
    public List<String> getAllowedTypes() { return allowedTypes; }
    public void setAllowedTypes(List<String> allowedTypes) { this.allowedTypes = allowedTypes; }
}
