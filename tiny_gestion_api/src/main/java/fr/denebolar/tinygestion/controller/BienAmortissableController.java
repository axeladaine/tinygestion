package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.domain.BienAmortissable;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.service.BienAmortissableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biens-amortissables")
@RequiredArgsConstructor
public class BienAmortissableController {

    private final BienAmortissableService service;

    @GetMapping("/logement/{logementId}")
    public ResponseEntity<List<BienAmortissable>> getBiensByLogement(
            @PathVariable Long logementId,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(service.getBiensByLogement(logementId, user));
    }

    @PostMapping
    public ResponseEntity<BienAmortissable> saveBien(
            @RequestBody BienAmortissable bien,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(service.saveBien(bien, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BienAmortissable> updateBien(
            @PathVariable Long id,
            @RequestBody BienAmortissable bien,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(service.updateBien(id, bien, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBien(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur user
    ) {
        service.deleteBien(id, user);
        return ResponseEntity.noContent().build();
    }
}
