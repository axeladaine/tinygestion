package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.domain.DocumentJustificatif;
import fr.denebolar.tinygestion.domain.TypeDocument;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.service.DocumentJustificatifService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents-justificatifs")
@RequiredArgsConstructor
public class DocumentJustificatifController {

    private final DocumentJustificatifService service;

    @PostMapping("/televerser")
    public ResponseEntity<DocumentJustificatif> televerserDocument(
            @RequestParam("fichier") MultipartFile file,
            @RequestParam("logementId") Long logementId,
            @RequestParam("typeDocument") TypeDocument typeDocument,
            @RequestParam(value = "entiteLieeType", required = false) String entiteLieeType,
            @RequestParam(value = "entiteLieeId", required = false) Long entiteLieeId,
            @AuthenticationPrincipal Utilisateur user
    ) {
        DocumentJustificatif doc = service.stockerDocument(file, logementId, typeDocument, entiteLieeType, entiteLieeId, user);
        return ResponseEntity.ok(doc);
    }

    @GetMapping("/logement/{logementId}")
    public ResponseEntity<List<DocumentJustificatif>> getDocuments(
            @PathVariable Long logementId,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(service.getDocumentsByLogement(logementId, user));
    }

    @GetMapping("/{id}/telecharger")
    public ResponseEntity<Resource> telechargerDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur user
    ) {
        Resource file = service.chargerFichier(id, user);
        DocumentJustificatif doc = service.getDocumentById(id, user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getNomFichier() + "\"")
                .contentType(MediaType.parseMediaType(doc.getTypeFichier() != null ? doc.getTypeFichier() : "application/octet-stream"))
                .body(file);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur user
    ) {
        service.supprimerDocument(id, user);
        return ResponseEntity.noContent().build();
    }
}
