package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.Depense;
import fr.denebolar.tinygestion.domain.Recette;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.dto.dashboard.StatsMensuellesDto;
import fr.denebolar.tinygestion.repository.DepenseRepository;
import fr.denebolar.tinygestion.repository.RecetteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TableauDeBordService {

    private final RecetteRepository recetteRepository;
    private final DepenseRepository depenseRepository;
    private final LogementService logementService;

    public StatsMensuellesDto getStatsMensuelles(Long logementId, int mois, int annee, Utilisateur user) {
        // Valider l'accès au logement
        logementService.getLogementById(logementId, user);

        LocalDate start = LocalDate.of(annee, mois, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Recette> recettes = recetteRepository.findByLogementIdAndDateEncaissementBetween(logementId, start, end);
        List<Depense> depenses = depenseRepository.findByLogementIdAndDateDepenseBetween(logementId, start, end);

        BigDecimal totalRecettes = recettes.stream()
                .map(r -> r.getMontantBrut() != null ? r.getMontantBrut() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDepenses = depenses.stream()
                .map(d -> d.getMontantRetenu() != null ? d.getMontantRetenu() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal resultat = totalRecettes.subtract(totalDepenses);

        // Justificatifs manquants (recettes ou dépenses sans justificatif associé)
        long recettesSansJustificatif = recettes.stream()
                .filter(r -> r.getDocumentJustificatif() == null)
                .count();

        long depensesSansJustificatif = depenses.stream()
                .filter(d -> d.getDocumentJustificatif() == null)
                .count();

        long totalManquants = recettesSansJustificatif + depensesSansJustificatif;

        return new StatsMensuellesDto(totalRecettes, totalDepenses, resultat, totalManquants);
    }
}
