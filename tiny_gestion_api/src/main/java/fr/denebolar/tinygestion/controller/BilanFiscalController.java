package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.dto.bilan.BilanFiscalDto;
import fr.denebolar.tinygestion.service.BilanFiscalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bilan-fiscal")
@RequiredArgsConstructor
public class BilanFiscalController {

    private final BilanFiscalService service;

    @GetMapping("/logement/{logementId}/annee/{annee}")
    public ResponseEntity<BilanFiscalDto> getBilanFiscal(
            @PathVariable Long logementId,
            @PathVariable Integer annee,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(service.calculerBilanFiscal(logementId, annee, user));
    }

}
