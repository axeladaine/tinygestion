package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.domain.Depense;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.service.DepenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/depenses")
@RequiredArgsConstructor
public class DepenseController {

    private final DepenseService depenseService;

    @GetMapping("/logement/{logementId}")
    public ResponseEntity<List<Depense>> getDepensesByLogement(
            @PathVariable Long logementId,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(depenseService.getDepensesByLogement(logementId, user));
    }

    @GetMapping("/logement/{logementId}/periode")
    public ResponseEntity<List<Depense>> getDepensesByLogementAndPeriod(
            @PathVariable Long logementId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(depenseService.getDepensesByLogementAndPeriod(logementId, debut, fin, user));
    }

    @PostMapping
    public ResponseEntity<Depense> saveDepense(
            @RequestBody Depense depense,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(depenseService.saveDepense(depense, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Depense> updateDepense(
            @PathVariable Long id,
            @RequestBody Depense depense,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(depenseService.updateDepense(id, depense, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepense(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur user
    ) {
        depenseService.deleteDepense(id, user);
        return ResponseEntity.noContent().build();
    }
}
