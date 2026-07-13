package com.xtensus.hrmanagementapi.medical.document.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class MedicalDocumentStorageProperties {

    private String medicalCertificatesPath;

    private DataSize maxFileSize;

    private List<String> allowedTypes = new ArrayList<>();
}
