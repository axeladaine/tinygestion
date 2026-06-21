package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.Logement;
import fr.denebolar.tinygestion.domain.Depense;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.repository.DepenseRepository;
import fr.denebolar.tinygestion.domain.DocumentJustificatif;
import fr.denebolar.tinygestion.repository.DocumentJustificatifRepository;
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
    private final DocumentJustificatifRepository documentRepository;

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
        Depense saved = depenseRepository.save(depense);

        // Mettre à jour le justificatif si présent
        if (depense.getDocumentJustificatif() != null && depense.getDocumentJustificatif().getId() != null) {
            DocumentJustificatif doc = documentRepository.findById(depense.getDocumentJustificatif().getId())
                    .orElseThrow(() -> new RuntimeException("Document introuvable"));
            doc.setEntiteLieeType("DEPENSE");
            doc.setEntiteLieeId(saved.getId());
            documentRepository.save(doc);
            saved.setDocumentJustificatif(doc);
        }

        return saved;
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

        // Gérer le changement de justificatif pour mettre à jour les métadonnées de liaison
        if (depense.getDocumentJustificatif() != null && 
            (depenseDetails.getDocumentJustificatif() == null || 
             !depenseDetails.getDocumentJustificatif().getId().equals(depense.getDocumentJustificatif().getId()))) {
            DocumentJustificatif ancienDoc = depense.getDocumentJustificatif();
            ancienDoc.setEntiteLieeType(null);
            ancienDoc.setEntiteLieeId(null);
            documentRepository.save(ancienDoc);
        }

        if (depenseDetails.getDocumentJustificatif() != null) {
            DocumentJustificatif nouveauDoc = documentRepository.findById(depenseDetails.getDocumentJustificatif().getId())
                    .orElseThrow(() -> new RuntimeException("Document introuvable"));
            nouveauDoc.setEntiteLieeType("DEPENSE");
            nouveauDoc.setEntiteLieeId(depense.getId());
            documentRepository.save(nouveauDoc);
            depense.setDocumentJustificatif(nouveauDoc);
        } else {
            depense.setDocumentJustificatif(null);
        }

        depense.setDateDepense(depenseDetails.getDateDepense());
        depense.setFournisseur(depenseDetails.getFournisseur());
        depense.setCategorie(depenseDetails.getCategorie());
        depense.setMontantTtc(depenseDetails.getMontantTtc());
        depense.setTauxAffectationLocation(depenseDetails.getTauxAffectationLocation());
        depense.setMoyenPaiement(depenseDetails.getMoyenPaiement());
        depense.setStatutDeductibilite(depenseDetails.getStatutDeductibilite());
        depense.setCommentaire(depenseDetails.getCommentaire());

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
