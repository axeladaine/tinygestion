package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.BienAmortissable;
import fr.denebolar.tinygestion.domain.Logement;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.repository.BienAmortissableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BienAmortissableService {

    private final BienAmortissableRepository repository;
    private final LogementService logementService;

    public List<BienAmortissable> getBiensByLogement(Long logementId, Utilisateur user) {
        logementService.getLogementById(logementId, user);
        return repository.findByLogementId(logementId);
    }

    @Transactional
    public BienAmortissable saveBien(BienAmortissable bien, Utilisateur user) {
        Logement logement = logementService.getLogementById(bien.getLogement().getId(), user);
        bien.setLogement(logement);
        return repository.save(bien);
    }

    @Transactional
    public BienAmortissable updateBien(Long id, BienAmortissable details, Utilisateur user) {
        BienAmortissable bien = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bien amortissable introuvable"));
        
        logementService.getLogementById(bien.getLogement().getId(), user);

        if (details.getLogement() != null && !details.getLogement().getId().equals(bien.getLogement().getId())) {
            Logement nouveauLogement = logementService.getLogementById(details.getLogement().getId(), user);
            bien.setLogement(nouveauLogement);
        }

        bien.setNom(details.getNom());
        bien.setCategorie(details.getCategorie());
        bien.setDateAchat(details.getDateAchat());
        bien.setDateMiseEnService(details.getDateMiseEnService());
        bien.setMontantTtc(details.getMontantTtc());
        bien.setTauxAffectationLocation(details.getTauxAffectationLocation());
        bien.setDureeAmortissementAns(details.getDureeAmortissementAns());
        bien.setAmortissementDeduitCumule(details.getAmortissementDeduitCumule());
        bien.setAmortissementNonUtiliseCumule(details.getAmortissementNonUtiliseCumule());
        bien.setPossedeFacture(details.getPossedeFacture());
        bien.setDocumentJustificatif(details.getDocumentJustificatif());
        bien.setCommentaire(details.getCommentaire());

        return repository.save(bien);
    }

    @Transactional
    public void deleteBien(Long id, Utilisateur user) {
        BienAmortissable bien = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bien amortissable introuvable"));
        logementService.getLogementById(bien.getLogement().getId(), user);
        repository.delete(bien);
    }
}
