package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.ChecklistMensuelle;
import fr.denebolar.tinygestion.domain.Logement;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.repository.ChecklistMensuelleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChecklistMensuelleService {

    private final ChecklistMensuelleRepository repository;
    private final LogementService logementService;

    private static final String TACHES_PAR_DEFAUT = "[" +
            "{\"id\":1,\"libelle\":\"Vérifier et enregistrer les recettes du mois (Airbnb, Booking, Direct...)\",\"termine\":false}," +
            "{\"id\":2,\"libelle\":\"Téléverser et lier les justificatifs (factures, reçus, relevés de plateforme)\",\"termine\":false}," +
            "{\"id\":3,\"libelle\":\"Saisir les charges courantes du mois (eau, électricité, assurances, commissions...)\",\"termine\":false}," +
            "{\"id\":4,\"libelle\":\"Valider la déductibilité et le taux d'affectation locative des dépenses du mois\",\"termine\":false}," +
            "{\"id\":5,\"libelle\":\"Vérifier le suivi des biens amortissables et l'état des équipements durables\",\"termine\":false}," +
            "{\"id\":6,\"libelle\":\"Pointer les échéances des impôts locaux (CFE, Taxe Foncière) et planifier leur paiement\",\"termine\":false}" +
            "]";

    @Transactional
    public ChecklistMensuelle getOrCreateChecklist(Long logementId, Integer annee, Integer mois, Utilisateur user) {
        // Vérifier les droits sur le logement
        Logement logement = logementService.getLogementById(logementId, user);

        Optional<ChecklistMensuelle> existante = repository.findByLogementIdAndAnneeAndMois(logementId, annee, mois);
        if (existante.isPresent()) {
            return existante.get();
        }

        // Créer une nouvelle checklist par défaut
        ChecklistMensuelle nouvelle = ChecklistMensuelle.builder()
                .logement(logement)
                .annee(annee)
                .mois(mois)
                .tachesJson(TACHES_PAR_DEFAUT)
                .tauxCompletion(BigDecimal.ZERO)
                .build();

        return repository.save(nouvelle);
    }

    @Transactional
    public ChecklistMensuelle updateChecklist(Long id, String newTachesJson, Utilisateur user) {
        ChecklistMensuelle checklist = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Checklist mensuelle introuvable"));

        // Vérifier les droits d'accès
        logementService.getLogementById(checklist.getLogement().getId(), user);

        checklist.setTachesJson(newTachesJson);
        checklist.setTauxCompletion(calculerTauxCompletion(newTachesJson));

        return repository.save(checklist);
    }

    public List<ChecklistMensuelle> getChecklistsByLogementAndAnnee(Long logementId, Integer annee, Utilisateur user) {
        logementService.getLogementById(logementId, user);
        return repository.findByLogementIdAndAnnee(logementId, annee);
    }

    /**
     * Calcule le taux de complétion basé sur le JSON des tâches.
     * Pour rester robuste et éviter une dépendance lourde de parsing JSON (comme Jackson Object Mapper complexe)
     * dans cette méthode simple, on compte tout simplement le nombre d'occurrences de '"termine":true' et '"termine":false'.
     */
    private BigDecimal calculerTauxCompletion(String json) {
        if (json == null || json.isBlank()) {
            return BigDecimal.ZERO;
        }

        int total = 0;
        int terminees = 0;

        // Compter les occurrences
        int index = 0;
        while ((index = json.indexOf("\"termine\":", index)) != -1) {
            total++;
            // Vérifier si le caractère après est 't' (true) ou 'f' (false)
            int valIndex = index + 10;
            if (valIndex < json.length() && json.charAt(valIndex) == 't') {
                terminees++;
            }
            index += 10;
        }

        if (total == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(terminees)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}
