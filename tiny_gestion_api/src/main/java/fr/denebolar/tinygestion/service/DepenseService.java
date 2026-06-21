package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.Logement;
import fr.denebolar.tinygestion.domain.Depense;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.repository.DepenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepenseService {

    private final DepenseRepository depenseRepository;
    private final LogementService logementService;

    public List<Depense> getDepensesByLogement(Long logementId, Utilisateur user) {
        logementService.getLogementById(logementId, user);
        return depenseRepository.findByLogementId(logementId);
    }

    public List<Depense> getDepensesByLogementAndPeriod(Long logementId, LocalDate start, LocalDate end, Utilisateur user) {
        logementService.getLogementById(logementId, user);
        return depenseRepository.findByLogementIdAndDateDepenseBetween(logementId, start, end);
    }

    @Transactional
    public Depense saveDepense(Depense depense, Utilisateur user) {
        Logement logement = logementService.getLogementById(depense.getLogement().getId(), user);
        depense.setLogement(logement);
        return depenseRepository.save(depense);
    }

    @Transactional
    public Depense updateDepense(Long id, Depense depenseDetails, Utilisateur user) {
        Depense depense = depenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dépense introuvable"));
        
        logementService.getLogementById(depense.getLogement().getId(), user);
        
        if (depenseDetails.getLogement() != null && !depenseDetails.getLogement().getId().equals(depense.getLogement().getId())) {
            Logement nouveauLogement = logementService.getLogementById(depenseDetails.getLogement().getId(), user);
            depense.setLogement(nouveauLogement);
        }

        depense.setDateDepense(depenseDetails.getDateDepense());
        depense.setFournisseur(depenseDetails.getFournisseur());
        depense.setCategorie(depenseDetails.getCategorie());
        depense.setMontantTtc(depenseDetails.getMontantTtc());
        depense.setTauxAffectationLocation(depenseDetails.getTauxAffectationLocation());
        depense.setMoyenPaiement(depenseDetails.getMoyenPaiement());
        depense.setStatutDeductibilite(depenseDetails.getStatutDeductibilite());
        depense.setCommentaire(depenseDetails.getCommentaire());
        depense.setDocumentJustificatif(depenseDetails.getDocumentJustificatif());

        return depenseRepository.save(depense);
    }

    @Transactional
    public void deleteDepense(Long id, Utilisateur user) {
        Depense depense = depenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dépense introuvable"));
        logementService.getLogementById(depense.getLogement().getId(), user);
        depenseRepository.delete(depense);
    }
}
