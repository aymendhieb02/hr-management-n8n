package com.xtensus.hrmanagementapi.employe.service;

import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.employe.exception.EmployeInvalideException;
import com.xtensus.hrmanagementapi.employe.exception.EmployeIntrouvableException;
import com.xtensus.hrmanagementapi.repository.EmployeRepository;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.unit.DataSize;

@Service
public class PhotoProfilService {
    private static final Set<String> TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final EmployeRepository repository;
    private final Path dossier;
    private final DataSize tailleMaximale;
    public PhotoProfilService(EmployeRepository repository,
            @Value("${app.storage.profile-photos-path:./storage/profile-photos}") String dossier,
            @Value("${app.storage.profile-photo-max-file-size:10MB}") DataSize tailleMaximale) {
        this.repository=repository; this.dossier=Path.of(dossier).toAbsolutePath().normalize(); this.tailleMaximale=tailleMaximale;
    }
    @Transactional
    public void enregistrer(Long employeId, MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) throw new EmployeInvalideException("La photo est obligatoire");
        if (!TYPES.contains(fichier.getContentType())) throw new EmployeInvalideException("Formats autorises : JPG, PNG ou WebP");
        if (fichier.getSize() > tailleMaximale.toBytes()) throw new EmployeInvalideException("La photo ne doit pas depasser " + tailleMaximale.toMegabytes() + " Mo");
        Employe employe = repository.findById(employeId).orElseThrow(() -> new EmployeIntrouvableException(employeId));
        String extension = switch (fichier.getContentType()) { case "image/png" -> ".png"; case "image/webp" -> ".webp"; default -> ".jpg"; };
        Path cible = dossier.resolve(UUID.randomUUID() + extension).normalize();
        try {
            Files.createDirectories(dossier); fichier.transferTo(cible);
            supprimerAncienne(employe.getPhotoProfil()); employe.setPhotoProfil(cible.toString()); repository.save(employe);
        } catch (Exception ex) { throw new EmployeInvalideException("Impossible d'enregistrer la photo de profil"); }
    }
    @Transactional(readOnly = true)
    public FileSystemResource lire(Long employeId) {
        Employe employe = repository.findById(employeId).orElseThrow(() -> new EmployeIntrouvableException(employeId));
        if (employe.getPhotoProfil() == null) throw new EmployeInvalideException("Cet employe n'a pas encore de photo");
        FileSystemResource resource = new FileSystemResource(employe.getPhotoProfil());
        if (!resource.exists()) throw new EmployeInvalideException("La photo de profil est introuvable");
        return resource;
    }
    private void supprimerAncienne(String chemin) { if (chemin != null) try { Files.deleteIfExists(Path.of(chemin)); } catch (Exception ignored) {} }
}
