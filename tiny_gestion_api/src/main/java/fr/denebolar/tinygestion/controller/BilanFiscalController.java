package fr.denebolar.tinygestion.controller;

import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.dto.bilan.BilanFiscalDto;
import fr.denebolar.tinygestion.service.BilanFiscalService;
import fr.denebolar.tinygestion.service.DeclarationPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final DeclarationPdfService pdfService;

    @GetMapping("/logement/{logementId}/annee/{annee}")
    public ResponseEntity<BilanFiscalDto> getBilanFiscal(
            @PathVariable Long logementId,
            @PathVariable Integer annee,
            @AuthenticationPrincipal Utilisateur user
    ) {
        return ResponseEntity.ok(service.calculerBilanFiscal(logementId, annee, user));
    }

    @GetMapping("/logement/{logementId}/annee/{annee}/export-pdf")
    public ResponseEntity<byte[]> exporterDeclarationPdf(
            @PathVariable Long logementId,
            @PathVariable Integer annee,
            @AuthenticationPrincipal Utilisateur user
    ) {
        byte[] pdfBytes = pdfService.genererDeclarationPdf(logementId, annee, user);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "declaration_fiscale_" + annee + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
