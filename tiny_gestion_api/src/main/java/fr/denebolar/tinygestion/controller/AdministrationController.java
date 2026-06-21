package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.domain.JournalSecurite;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.dto.auth.CreerAssistantRequest;
import fr.denebolar.tinygestion.dto.auth.UtilisateurDto;
import fr.denebolar.tinygestion.service.AdministrationService;
import fr.denebolar.tinygestion.service.JournalSecuriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/administration")
@RequiredArgsConstructor
public class AdministrationController {

    private final AdministrationService administrationService;
    private final JournalSecuriteService journalSecuriteService;

    @GetMapping("/journal-securite")
    public ResponseEntity<List<JournalSecurite>> getJournalSecurite(
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(journalSecuriteService.getTousLesEvenements(user));
    }

    @PostMapping("/assistant")
    public ResponseEntity<UtilisateurDto> creerAssistant(
            @RequestBody CreerAssistantRequest request,
            @AuthenticationPrincipal Utilisateur user,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = servletRequest.getRemoteAddr();
        return ResponseEntity.ok(administrationService.creerAssistant(request, user, ipAddress));
    }

    @GetMapping("/assistant")
    public ResponseEntity<List<UtilisateurDto>> getAssistants(
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(administrationService.getAssistants(user));
    }

    @DeleteMapping("/assistant/{id}")
    public ResponseEntity<Void> supprimerAssistant(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur user,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = servletRequest.getRemoteAddr();
        administrationService.supprimerAssistant(id, user, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
