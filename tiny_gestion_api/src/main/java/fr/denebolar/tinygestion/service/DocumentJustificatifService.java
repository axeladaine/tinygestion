package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.DocumentJustificatif;
import fr.denebolar.tinygestion.domain.Logement;
import fr.denebolar.tinygestion.domain.TypeDocument;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.repository.DocumentJustificatifRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentJustificatifService {

    private final DocumentJustificatifRepository documentRepository;
    private final LogementService logementService;

    // Répertoire de stockage relatif
    private final Path rootLocation = Paths.get("uploads/documents");

    @Transactional
    public DocumentJustificatif stockerDocument(
            MultipartFile file,
            Long logementId,
            TypeDocument typeDocument,
            String entiteLieeType,
            Long entiteLieeId,
            Utilisateur user
    ) {
        // Valider le logement et les droits
        Logement logement = logementService.getLogementById(logementId, user);

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Le fichier est vide.");
            }

            // Valider le type mime / extension
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = "";
            int i = originalFilename.lastIndexOf('.');
            if (i > 0) {
                extension = originalFilename.substring(i + 1).toLowerCase();
            }

            if (!extension.equals("pdf") && !extension.equals("png") && !extension.equals("jpg") && !extension.equals("jpeg")) {
                throw new RuntimeException("Seuls les formats PDF, PNG et JPG sont acceptés.");
            }

            // S'assurer que le répertoire existe
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }

            // Générer un nom de stockage unique
            String storageFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path destinationFile = this.rootLocation.resolve(Paths.get(storageFilename)).normalize().toAbsolutePath();

            // Sécurité contre la traversée de répertoire
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Impossible de stocker le fichier en dehors du répertoire cible.");
            }

            // Sauvegarder le fichier physiquement
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Créer la ligne de métadonnées
            DocumentJustificatif document = DocumentJustificatif.builder()
                    .logement(logement)
                    .nomFichier(originalFilename)
                    .typeFichier(file.getContentType())
                    .cheminStockage(storageFilename)
                    .typeDocument(typeDocument)
                    .entiteLieeType(entiteLieeType)
                    .entiteLieeId(entiteLieeId)
                    .build();

            return documentRepository.save(document);

        } catch (IOException e) {
            throw new RuntimeException("Erreur d'écriture du fichier.", e);
        }
    }

    public List<DocumentJustificatif> getDocumentsByLogement(Long logementId, Utilisateur user) {
        logementService.getLogementById(logementId, user);
        return documentRepository.findByLogementId(logementId);
    }

    public DocumentJustificatif getDocumentById(Long id, Utilisateur user) {
        DocumentJustificatif doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));
        
        // Vérifier l'accès au logement associé
        logementService.getLogementById(doc.getLogement().getId(), user);
        return doc;
    }

    public Resource chargerFichier(Long id, Utilisateur user) {
        DocumentJustificatif doc = getDocumentById(id, user);
        try {
            Path file = rootLocation.resolve(doc.getCheminStockage());
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier introuvable sur le disque : " + doc.getNomFichier());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur de chargement du fichier : " + doc.getNomFichier(), e);
        }
    }

    @Transactional
    public void supprimerDocument(Long id, Utilisateur user) {
        DocumentJustificatif doc = getDocumentById(id, user);

        // Supprimer le fichier sur le disque
        try {
            Path file = rootLocation.resolve(doc.getCheminStockage());
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Loguer et continuer la suppression logique même si le fichier physique est déjà manquant
            System.err.println("Fichier physique manquant ou non supprimable : " + e.getMessage());
        }

        // Supprimer l'enregistrement en base
        documentRepository.delete(doc);
    }
}
