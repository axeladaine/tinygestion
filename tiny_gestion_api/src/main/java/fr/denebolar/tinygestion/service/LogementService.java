package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.Logement;
import fr.denebolar.tinygestion.domain.Proprietaire;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.repository.LogementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogementService {

    private final LogementRepository logementRepository;

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
}
