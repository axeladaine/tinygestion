package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.dto.logement.InitialisationLogementDto;
import fr.denebolar.tinygestion.repository.DepenseRepository;
import fr.denebolar.tinygestion.repository.LogementRepository;
import fr.denebolar.tinygestion.repository.RecetteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogementService {

    private final LogementRepository logementRepository;
    private final RecetteRepository recetteRepository;
    private final DepenseRepository depenseRepository;

    private Proprietaire getProprietaireFromUser(Utilisateur user) {
        if (user.getProprietaire() != null) {
            return user.getProprietaire();
        }
        return null;
    }

    public List<Logement> getLogements(Utilisateur connectedUser) {
        Proprietaire proprietaire = getProprietaireFromUser(connectedUser);
        if (proprietaire == null) {
            return Collections.emptyList();
        }
        return logementRepository.findByProprietaireId(proprietaire.getId());
    }

    public Logement getLogementById(Long id, Utilisateur connectedUser) {
        Logement logement = logementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Logement introuvable"));
        
        Proprietaire proprietaire = getProprietaireFromUser(connectedUser);
        if (proprietaire == null || !logement.getProprietaire().getId().equals(proprietaire.getId())) {
            throw new RuntimeException("Accès non autorisé à ce logement");
        }
        return logement;
    }

    @Transactional
    public Logement saveLogement(Logement logement, Utilisateur connectedUser) {
        Proprietaire proprietaire = getProprietaireFromUser(connectedUser);
        if (proprietaire == null) {
            throw new RuntimeException("L'utilisateur connecté n'est pas associé à un propriétaire");
        }
        logement.setProprietaire(proprietaire);
        logement.setInitialise(false); // Par défaut non initialisé pour les nouveaux logements
        return logementRepository.save(logement);
    }

    @Transactional
    public Logement updateLogement(Long id, Logement logementDetails, Utilisateur connectedUser) {
        Logement logement = getLogementById(id, connectedUser);
        logement.setNom(logementDetails.getNom());
        logement.setAdresse(logementDetails.getAdresse());
        logement.setCodePostal(logementDetails.getCodePostal());
        logement.setVille(logementDetails.getVille());
        logement.setQualificationLogement(logementDetails.getQualificationLogement());
        logement.setEstDeplacable(logementDetails.isEstDeplacable());
        logement.setEstSurTerrainResidencePrincipale(logementDetails.isEstSurTerrainResidencePrincipale());
        logement.setEstLoueCourteDuree(logementDetails.isEstLoueCourteDuree());
        logement.setEstMeubleTourisme(logementDetails.isEstMeubleTourisme());
        logement.setDateDebutLocation(logementDetails.getDateDebutLocation());
        return logementRepository.save(logement);
    }

    @Transactional
    public Logement initialiserLogement(Long id, InitialisationLogementDto dto, Utilisateur connectedUser) {
        Logement logement = getLogementById(id, connectedUser);
        logement.setInitialise(true);
        Logement savedLogement = logementRepository.save(logement);

        int anneeCourante = LocalDate.now().getYear();
        LocalDate dateInit = LocalDate.of(anneeCourante, 1, 1);

        if (dto.recettesAnterieures() != null && dto.recettesAnterieures().compareTo(BigDecimal.ZERO) > 0) {
            Recette recette = Recette.builder()
                    .logement(savedLogement)
                    .montantBrut(dto.recettesAnterieures())
                    .dateEncaissement(dateInit)
                    .plateforme(Plateforme.AUTRE)
                    .nomClient("Initialisation Didacticiel")
                    .commentaire("Historique Recettes - Initialisation " + anneeCourante)
                    .build();
            recetteRepository.save(recette);
        }

        if (dto.depensesAnterieures() != null && dto.depensesAnterieures().compareTo(BigDecimal.ZERO) > 0) {
            Depense depense = Depense.builder()
                    .logement(savedLogement)
                    .montantTtc(dto.depensesAnterieures())
                    .dateDepense(dateInit)
                    .fournisseur("Initialisation Didacticiel")
                    .commentaire("Historique Dépenses - Initialisation " + anneeCourante)
                    .statutDeductibilite(StatutDeductibilite.DEDUCTIBLE)
                    .montantRetenu(dto.depensesAnterieures())
                    .build();
            depenseRepository.save(depense);
        }

        return savedLogement;
    }
}
