package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.domain.Logement;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.service.LogementService;
import fr.denebolar.tinygestion.dto.logement.InitialisationLogementDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logements")
@RequiredArgsConstructor
public class LogementController {

    private final LogementService logementService;

    @GetMapping
    public ResponseEntity<List<Logement>> getLogements(@AuthenticationPrincipal Utilisateur user) {
        return ResponseEntity.ok(logementService.getLogements(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Logement> getLogementById(@PathVariable Long id, @AuthenticationPrincipal Utilisateur user) {
        return ResponseEntity.ok(logementService.getLogementById(id, user));
    }

    @PostMapping
    public ResponseEntity<Logement> saveLogement(@RequestBody Logement logement, @AuthenticationPrincipal Utilisateur user) {
        return ResponseEntity.ok(logementService.saveLogement(logement, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Logement> updateLogement(
            @PathVariable Long id,
            @RequestBody Logement logement,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(logementService.updateLogement(id, logement, user));
    }

    @PostMapping("/{id}/initialiser")
    public ResponseEntity<Logement> initialiserLogement(
            @PathVariable Long id,
            @RequestBody InitialisationLogementDto dto,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(logementService.initialiserLogement(id, dto, user));
    }
}
