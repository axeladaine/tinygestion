package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.domain.Recette;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.service.RecetteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recettes")
@RequiredArgsConstructor
public class RecetteController {

    private final RecetteService recetteService;

    @GetMapping("/logement/{logementId}")
    public ResponseEntity<List<Recette>> getRecettesByLogement(
            @PathVariable Long logementId,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(recetteService.getRecettesByLogement(logementId, user));
    }

    @GetMapping("/logement/{logementId}/periode")
    public ResponseEntity<List<Recette>> getRecettesByLogementAndPeriod(
            @PathVariable Long logementId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(recetteService.getRecettesByLogementAndPeriod(logementId, debut, fin, user));
    }

    @PostMapping
    public ResponseEntity<Recette> saveRecette(
            @RequestBody Recette recette,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(recetteService.saveRecette(recette, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recette> updateRecette(
            @PathVariable Long id,
            @RequestBody Recette recette,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(recetteService.updateRecette(id, recette, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecette(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur user
    ) {
        recetteService.deleteRecette(id, user);
        return ResponseEntity.noContent().build();
    }
}
