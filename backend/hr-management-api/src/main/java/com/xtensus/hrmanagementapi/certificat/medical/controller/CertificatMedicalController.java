package com.xtensus.hrmanagementapi.certificat.medical.controller;

import com.xtensus.hrmanagementapi.certificat.medical.dto.CertificatMedicalResponse;
import com.xtensus.hrmanagementapi.certificat.medical.service.CertificatMedicalDownload;
import com.xtensus.hrmanagementapi.certificat.medical.service.CertificatMedicalService;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.xtensus.hrmanagementapi.security.user.CustomUserDetails;

@RestController
@RequestMapping("/api/certificats-medicaux-v2")
public class CertificatMedicalController {
    private final CertificatMedicalService service;

    public CertificatMedicalController(CertificatMedicalService service) {
        this.service = service;
    }

    @PostMapping(value = "/televerser/{congeDemandeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificatMedicalResponse> televerser(@PathVariable Long congeDemandeId, @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.televerser(congeDemandeId, file, principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificatMedicalResponse> trouverParId(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(service.trouverParId(id, principal));
    }

    @GetMapping("/demande/{congeDemandeId}")
    public ResponseEntity<CertificatMedicalResponse> trouverParDemande(@PathVariable Long congeDemandeId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return service.trouverParDemande(congeDemandeId, principal).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<CertificatMedicalResponse>> parEmploye(@PathVariable Long employeId, @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(service.parEmploye(employeId, principal));
    }

    @GetMapping("/telecharger/{id}")
    public ResponseEntity<Resource> telecharger(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        CertificatMedicalDownload download = service.telecharger(id, principal);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.certificat().getTypeMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.certificat().getNomFichier())
                        .build()
                        .toString())
                .body(download.resource());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
