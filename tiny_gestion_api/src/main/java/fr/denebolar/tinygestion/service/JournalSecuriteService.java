package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.JournalSecurite;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.repository.JournalSecuriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalSecuriteService {

    private final JournalSecuriteRepository repository;

    @Transactional
    public void enregistrerEvenement(
            Utilisateur utilisateur,
            String email,
            String typeEvenement,
            String adresseIp,
            String details
    ) {
        JournalSecurite event = JournalSecurite.builder()
                .utilisateur(utilisateur)
                .email(email != null ? email : (utilisateur != null ? utilisateur.getEmail() : null))
                .typeEvenement(typeEvenement)
                .adresseIp(adresseIp)
                .details(details)
                .build();
        repository.save(event);
    }

    public List<JournalSecurite> getTousLesEvenements(Utilisateur connectedUser) {
        if (!connectedUser.getRole().name().equals("PROPRIETAIRE")) {
            throw new RuntimeException("Accès réservé au propriétaire");
        }
        return repository.findAllByOrderByDateEvenementDesc();
    }
}
