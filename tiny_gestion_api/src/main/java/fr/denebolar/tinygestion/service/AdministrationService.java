package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.Proprietaire;
import fr.denebolar.tinygestion.domain.Role;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.dto.auth.CreerAssistantRequest;
import fr.denebolar.tinygestion.dto.auth.UtilisateurDto;
import fr.denebolar.tinygestion.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdministrationService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JournalSecuriteService journalSecuriteService;

    @Transactional
    public UtilisateurDto creerAssistant(CreerAssistantRequest request, Utilisateur connectedUser, String ipAddress) {
        if (!connectedUser.getRole().name().equals("PROPRIETAIRE")) {
            throw new RuntimeException("Accès refusé : Seuls les propriétaires peuvent gérer les assistants.");
        }

        Proprietaire proprietaire = connectedUser.getProprietaire();
        if (proprietaire == null) {
            throw new RuntimeException("Propriétaire introuvable pour l'utilisateur connecté.");
        }

        if (utilisateurRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur.");
        }

        Utilisateur assistant = Utilisateur.builder()
                .email(request.email())
                .motDePasseHash(passwordEncoder.encode(request.motDePasse()))
                .prenom(request.prenom())
                .nom(request.nom())
                .role(Role.ASSISTANT)
                .actif(true)
                .proprietaire(proprietaire)
                .build();

        assistant = utilisateurRepository.save(assistant);

        journalSecuriteService.enregistrerEvenement(
                connectedUser,
                connectedUser.getEmail(),
                "AJOUT_ASSISTANT",
                ipAddress,
                "Création du compte assistant : " + assistant.getEmail()
        );

        return new UtilisateurDto(
                assistant.getId().toString(),
                assistant.getNom(),
                assistant.getPrenom(),
                assistant.getEmail(),
                assistant.getRole().name()
        );
    }

    public List<UtilisateurDto> getAssistants(Utilisateur connectedUser) {
        if (!connectedUser.getRole().name().equals("PROPRIETAIRE")) {
            throw new RuntimeException("Accès refusé.");
        }
        Proprietaire proprietaire = connectedUser.getProprietaire();
        if (proprietaire == null) {
            throw new RuntimeException("Propriétaire introuvable.");
        }

        return utilisateurRepository.findByProprietaireId(proprietaire.getId()).stream()
                .filter(u -> u.getRole() == Role.ASSISTANT)
                .map(u -> new UtilisateurDto(
                        u.getId().toString(),
                        u.getNom(),
                        u.getPrenom(),
                        u.getEmail(),
                        u.getRole().name()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void supprimerAssistant(Long id, Utilisateur connectedUser, String ipAddress) {
        if (!connectedUser.getRole().name().equals("PROPRIETAIRE")) {
            throw new RuntimeException("Accès refusé.");
        }
        Proprietaire proprietaire = connectedUser.getProprietaire();
        if (proprietaire == null) {
            throw new RuntimeException("Propriétaire introuvable.");
        }

        Utilisateur assistant = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assistant introuvable"));

        if (assistant.getProprietaire() == null || !assistant.getProprietaire().getId().equals(proprietaire.getId())) {
            throw new RuntimeException("Accès refusé : Cet assistant n'est pas lié à votre compte.");
        }

        utilisateurRepository.delete(assistant);

        journalSecuriteService.enregistrerEvenement(
                connectedUser,
                connectedUser.getEmail(),
                "SUPPRESSION_ASSISTANT",
                ipAddress,
                "Suppression du compte assistant : " + assistant.getEmail()
        );
    }
}
