package com.xtensus.hrmanagementapi.certificat.medical.service;

import com.xtensus.hrmanagementapi.certificat.medical.dto.CertificatMedicalResponse;
import com.xtensus.hrmanagementapi.certificat.medical.exception.CertificatMedicalExisteDejaException;
import com.xtensus.hrmanagementapi.certificat.medical.exception.CertificatMedicalIntrouvableException;
import com.xtensus.hrmanagementapi.certificat.medical.exception.CertificatMedicalInvalideException;
import com.xtensus.hrmanagementapi.certificat.medical.mapper.CertificatMedicalMapper;
import com.xtensus.hrmanagementapi.conge.demande.exception.CongeDemandeIntrouvableException;
import com.xtensus.hrmanagementapi.domain.entity.CongeDemande;
import com.xtensus.hrmanagementapi.domain.entity.EmployeCertificatMedical;
import com.xtensus.hrmanagementapi.certificat.medical.config.CertificatMedicalStorageProperties;
import com.xtensus.hrmanagementapi.certificat.medical.exception.StorageException;
import com.xtensus.hrmanagementapi.repository.CongeDemandeRepository;
import com.xtensus.hrmanagementapi.repository.EmployeCertificatMedicalRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CertificatMedicalService {
    private static final Set<String> TYPES_AUTORISES = Set.of("application/pdf", "image/jpeg", "image/png");
    private final EmployeCertificatMedicalRepository repository;
    private final CongeDemandeRepository demandes;
    private final CertificatMedicalStorageProperties properties;
    private final CertificatMedicalMapper mapper;

    public CertificatMedicalService(EmployeCertificatMedicalRepository repository, CongeDemandeRepository demandes,
            CertificatMedicalStorageProperties properties, CertificatMedicalMapper mapper) {
        this.repository = repository;
        this.demandes = demandes;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Transactional
    public CertificatMedicalResponse televerser(Long congeDemandeId, MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) throw new CertificatMedicalInvalideException("Le fichier est obligatoire");
        if (!TYPES_AUTORISES.contains(fichier.getContentType())) throw new CertificatMedicalInvalideException("Type de fichier non autorise");
        if (fichier.getSize() > properties.getMaxFileSize().toBytes()) throw new CertificatMedicalInvalideException("Le fichier depasse la taille maximale autorisee");
        if (repository.existsByCongeDemandeId(congeDemandeId)) throw new CertificatMedicalExisteDejaException(congeDemandeId);

        CongeDemande demande = demandes.findById(congeDemandeId).orElseThrow(() -> new CongeDemandeIntrouvableException(congeDemandeId));
        String extension = extension(fichier.getOriginalFilename());
        String nomStocke = UUID.randomUUID() + extension;
        Path dossier = Path.of(properties.getMedicalCertificatesPath());
        Path cible = dossier.resolve(nomStocke).normalize();
        try {
            Files.createDirectories(dossier);
            fichier.transferTo(cible);
        } catch (IOException ex) {
            throw new StorageException("Impossible de stocker le certificat medical");
        }

        EmployeCertificatMedical certificat = new EmployeCertificatMedical();
        certificat.setCongeDemande(demande);
        certificat.setNomFichier(fichier.getOriginalFilename());
        certificat.setLienFichier(cible.toString());
        certificat.setTypeMime(fichier.getContentType());
        certificat.setTailleFichier(fichier.getSize());
        certificat.setDateSoumission(LocalDateTime.now());
        return mapper.toResponse(repository.save(certificat));
    }

    @Transactional(readOnly = true)
    public CertificatMedicalResponse trouverParId(Long id) {
        return mapper.toResponse(entite(id));
    }

    @Transactional(readOnly = true)
    public List<CertificatMedicalResponse> parEmploye(Long employeId) {
        return repository.findByCongeDemandeEmployeId(employeId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CertificatMedicalDownload telecharger(Long id) {
        EmployeCertificatMedical certificat = entite(id);
        FileSystemResource resource = new FileSystemResource(certificat.getLienFichier());
        if (!resource.exists()) throw new StorageException("Fichier introuvable sur le stockage local");
        return new CertificatMedicalDownload(certificat, resource);
    }

    @Transactional
    public void supprimer(Long id) {
        EmployeCertificatMedical certificat = entite(id);
        try {
            Files.deleteIfExists(Path.of(certificat.getLienFichier()));
        } catch (IOException ex) {
            throw new StorageException("Impossible de supprimer le certificat medical");
        }
        repository.delete(certificat);
    }

    private EmployeCertificatMedical entite(Long id) {
        return repository.findById(id).orElseThrow(() -> new CertificatMedicalIntrouvableException(id));
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
