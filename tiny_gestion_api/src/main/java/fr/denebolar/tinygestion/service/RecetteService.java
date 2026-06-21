package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.Logement;
import fr.denebolar.tinygestion.domain.Recette;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.repository.RecetteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecetteService {

    private final RecetteRepository recetteRepository;
    private final LogementService logementService;

    public List<Recette> getRecettesByLogement(Long logementId, Utilisateur user) {
        // Validation que le logement appartient à l'utilisateur
        logementService.getLogementById(logementId, user);
        return recetteRepository.findByLogementId(logementId);
    }

    public List<Recette> getRecettesByLogementAndPeriod(Long logementId, LocalDate start, LocalDate end, Utilisateur user) {
        logementService.getLogementById(logementId, user);
        return recetteRepository.findByLogementIdAndDateEncaissementBetween(logementId, start, end);
    }

    @Transactional
    public Recette saveRecette(Recette recette, Utilisateur user) {
        // Valider que le logement appartient à l'utilisateur
        Logement logement = logementService.getLogementById(recette.getLogement().getId(), user);
        recette.setLogement(logement);
        return recetteRepository.save(recette);
    }

    @Transactional
    public Recette updateRecette(Long id, Recette recetteDetails, Utilisateur user) {
        Recette recette = recetteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recette introuvable"));
        
        // Valider l'accès au logement existant
        logementService.getLogementById(recette.getLogement().getId(), user);
        
        // Valider l'accès au nouveau logement si modifié
        if (recetteDetails.getLogement() != null && !recetteDetails.getLogement().getId().equals(recette.getLogement().getId())) {
            Logement nouveauLogement = logementService.getLogementById(recetteDetails.getLogement().getId(), user);
            recette.setLogement(nouveauLogement);
        }

        recette.setPlateforme(recetteDetails.getPlateforme());
        recette.setNomClient(recetteDetails.getNomClient());
        recette.setDateDebutSejour(recetteDetails.getDateDebutSejour());
        recette.setDateFinSejour(recetteDetails.getDateFinSejour());
        recette.setDateEncaissement(recetteDetails.getDateEncaissement());
        recette.setNombreNuits(recetteDetails.getNombreNuits());
        recette.setMontantBrut(recetteDetails.getMontantBrut());
        recette.setFraisPlateforme(recetteDetails.getFraisPlateforme());
        recette.setMontantTaxeSejour(recetteDetails.getMontantTaxeSejour());
        recette.setCommentaire(recetteDetails.getCommentaire());
        recette.setDocumentJustificatif(recetteDetails.getDocumentJustificatif());

        return recetteRepository.save(recette);
    }

    @Transactional
    public void deleteRecette(Long id, Utilisateur user) {
        Recette recette = recetteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recette introuvable"));
        logementService.getLogementById(recette.getLogement().getId(), user);
        recetteRepository.delete(recette);
    }
}
