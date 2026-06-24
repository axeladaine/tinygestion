package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.dto.bilan.BilanFiscalDto;
import fr.denebolar.tinygestion.repository.BienAmortissableRepository;
import fr.denebolar.tinygestion.repository.DepenseRepository;
import fr.denebolar.tinygestion.repository.RecetteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BilanFiscalService {

    private final RecetteRepository recetteRepository;
    private final DepenseRepository depenseRepository;
    private final BienAmortissableRepository bienAmortissableRepository;
    private final LogementService logementService;

    public BilanFiscalDto calculerBilanFiscal(Long logementId, Integer annee, Utilisateur user) {
        // Valider le logement et les droits
        Logement logement = logementService.getLogementById(logementId, user);

        LocalDate start = LocalDate.of(annee, 1, 1);
        LocalDate end = LocalDate.of(annee, 12, 31);

        // 1. Recettes
        List<Recette> recettes = recetteRepository.findByLogementIdAndDateEncaissementBetween(logementId, start, end);
        BigDecimal recettesBrutes = recettes.stream()
                .map(r -> r.getMontantBrut() != null ? r.getMontantBrut() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Dépenses déductibles
        List<Depense> depenses = depenseRepository.findByLogementIdAndDateDepenseBetween(logementId, start, end);
        BigDecimal depensesRetenues = depenses.stream()
                .filter(d -> d.getStatutDeductibilite() == StatutDeductibilite.DEDUCTIBLE)
                .map(d -> d.getMontantRetenu() != null ? d.getMontantRetenu() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Amortissements éligibles pour cette année
        List<BienAmortissable> biens = bienAmortissableRepository.findByLogementId(logementId);
        BigDecimal amortissementDisponible = biens.stream()
                .filter(b -> {
                    LocalDate dateDebut = b.getDateMiseEnService() != null ? b.getDateMiseEnService() : b.getDateAchat();
                    if (dateDebut == null) return false;
                    int anneeDebut = dateDebut.getYear();
                    int duree = b.getDureeAmortissementAns() != null ? b.getDureeAmortissementAns() : 1;
                    return annee >= anneeDebut && annee < (anneeDebut + duree);
                })
                .map(b -> b.getAmortissementAnnuel() != null ? b.getAmortissementAnnuel() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Calculs du régime réel
        // resultat_avant_amortissement = recettes_annuelles - depenses_retenues_annuelles
        BigDecimal resultatAvantAmortissement = recettesBrutes.subtract(depensesRetenues);

        // amortissement_utilise = minimum(amortissement_disponible, resultat_avant_amortissement)
        BigDecimal amortissementUtilise = BigDecimal.ZERO;
        if (resultatAvantAmortissement.compareTo(BigDecimal.ZERO) > 0) {
            amortissementUtilise = amortissementDisponible.min(resultatAvantAmortissement);
        }

        // resultat_reel_estimatif = resultat_avant_amortissement - amortissement_utilise
        BigDecimal resultatReelImposable = resultatAvantAmortissement.subtract(amortissementUtilise);
        if (resultatReelImposable.compareTo(BigDecimal.ZERO) < 0) {
            resultatReelImposable = BigDecimal.ZERO;
        }

        // amortissement_non_utilise = amortissement_disponible - amortissement_utilise
        BigDecimal amortissementNonUtilise = amortissementDisponible.subtract(amortissementUtilise);

        // 5. Calculs du régime Micro-BIC
        boolean estMeubleTourisme = logement.getQualificationLogement() == QualificationLogement.MEUBLE_DE_TOURISME
                || logement.isEstMeubleTourisme();
        boolean estLoueCourteDuree = logement.isEstLoueCourteDuree();

        BigDecimal tauxAbattement;
        BigDecimal seuilMicroBic;
        String caseFiscaleMicroBic;

        if (estLoueCourteDuree) {
            if (estMeubleTourisme) {
                // Saisonnier classé
                tauxAbattement = BigDecimal.valueOf(50); // Loi Le Meur 2026 : classé = 50%
                seuilMicroBic = BigDecimal.valueOf(83600); // 83 600 €
                caseFiscaleMicroBic = "5NG";
            } else {
                // Saisonnier non classé (Airbnb standard)
                tauxAbattement = BigDecimal.valueOf(30); // Loi Le Meur 2026 : non classé = 30%
                seuilMicroBic = BigDecimal.valueOf(15000); // 15 000 €
                caseFiscaleMicroBic = "5NH";
            }
        } else {
            // Location classique longue durée
            tauxAbattement = BigDecimal.valueOf(50);
            seuilMicroBic = BigDecimal.valueOf(83600); // 83 600 €
            caseFiscaleMicroBic = "5NI";
        }

        BigDecimal abattementMicroBic = recettesBrutes.multiply(tauxAbattement)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal resultatMicroBicImposable = recettesBrutes.subtract(abattementMicroBic);
        if (resultatMicroBicImposable.compareTo(BigDecimal.ZERO) < 0) {
            resultatMicroBicImposable = BigDecimal.ZERO;
        }

        // 6. Arbitrage fiscal
        String regimeFiscalConseille;
        BigDecimal differenceGainImposable;

        // Le régime le plus avantageux est celui dont la base imposable est la plus basse
        // MAIS si les recettes brutes dépassent le seuil légal du Micro-BIC, le régime réel est obligatoire.
        if (recettesBrutes.compareTo(seuilMicroBic) > 0) {
            regimeFiscalConseille = "REGIME_REEL_AVANTAGEUX"; // Forcé car micro-BIC non autorisé
            differenceGainImposable = BigDecimal.ZERO;
        } else {
            if (resultatReelImposable.compareTo(resultatMicroBicImposable) < 0) {
                regimeFiscalConseille = "REGIME_REEL_AVANTAGEUX";
                differenceGainImposable = resultatMicroBicImposable.subtract(resultatReelImposable);
            } else if (resultatReelImposable.compareTo(resultatMicroBicImposable) > 0) {
                regimeFiscalConseille = "MICRO_BIC_AVANTAGEUX";
                differenceGainImposable = resultatReelImposable.subtract(resultatMicroBicImposable);
            } else {
                regimeFiscalConseille = "EQUIVALENT";
                differenceGainImposable = BigDecimal.ZERO;
            }
        }

        return new BilanFiscalDto(
                logementId,
                annee,
                recettesBrutes,
                depensesRetenues,
                amortissementDisponible,
                resultatAvantAmortissement,
                amortissementUtilise,
                resultatReelImposable,
                amortissementNonUtilise,
                estMeubleTourisme,
                abattementMicroBic,
                resultatMicroBicImposable,
                regimeFiscalConseille,
                differenceGainImposable,
                estLoueCourteDuree,
                caseFiscaleMicroBic
        );
    }
}
