package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.dto.dashboard.StatsMensuellesDto;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.service.TableauDeBordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tableau-de-bord")
@RequiredArgsConstructor
public class TableauDeBordController {

    private final TableauDeBordService dashboardService;

    @GetMapping("/logement/{logementId}")
    public ResponseEntity<StatsMensuellesDto> getStatsMensuelles(
            @PathVariable Long logementId,
            @RequestParam int mois,
            @RequestParam int annee,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(dashboardService.getStatsMensuelles(logementId, mois, annee, user));
    }
}
