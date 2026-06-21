package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.domain.ChecklistMensuelle;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.service.ChecklistMensuelleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checklists-mensuelles")
@RequiredArgsConstructor
public class ChecklistMensuelleController {

    private final ChecklistMensuelleService service;

    @GetMapping("/logement/{logementId}/annee/{annee}/mois/{mois}")
    public ResponseEntity<ChecklistMensuelle> getOrCreateChecklist(
            @PathVariable Long logementId,
            @PathVariable Integer annee,
            @PathVariable Integer mois,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(service.getOrCreateChecklist(logementId, annee, mois, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChecklistMensuelle> updateChecklist(
            @PathVariable Long id,
            @RequestBody String newTachesJson,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(service.updateChecklist(id, newTachesJson, user));
    }

    @GetMapping("/logement/{logementId}/annee/{annee}")
    public ResponseEntity<List<ChecklistMensuelle>> getChecklistsByLogementAndAnnee(
            @PathVariable Long logementId,
            @PathVariable Integer annee,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(service.getChecklistsByLogementAndAnnee(logementId, annee, user));
    }
}
