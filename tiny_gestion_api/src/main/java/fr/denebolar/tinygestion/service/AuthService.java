package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.Logement;
import fr.denebolar.tinygestion.domain.Proprietaire;
import fr.denebolar.tinygestion.domain.QualificationLogement;
import fr.denebolar.tinygestion.domain.Role;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.dto.auth.AuthRequest;
import fr.denebolar.tinygestion.dto.auth.AuthResponse;
import fr.denebolar.tinygestion.dto.auth.InscriptionRequest;
import fr.denebolar.tinygestion.dto.auth.UtilisateurDto;
import fr.denebolar.tinygestion.repository.LogementRepository;
import fr.denebolar.tinygestion.repository.ProprietaireRepository;
import fr.denebolar.tinygestion.repository.UtilisateurRepository;
import fr.denebolar.tinygestion.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProprietaireRepository proprietaireRepository;
    private final LogementRepository logementRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JournalSecuriteService journalSecuriteService;

    @Transactional
    public AuthResponse login(AuthRequest request, String ipAddress) {
        log.info("Tentative de connexion pour l'email : {}", request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.motDePasse()
                    )
            );
            log.info("Authentification Spring Security réussie pour : {}", request.email());
        } catch (AuthenticationException e) {
            log.error("Échec de l'authentification Spring Security pour {} : {}", request.email(), e.getMessage());
            journalSecuriteService.enregistrerEvenement(null, request.email(), "CONNEXION_ECHOUEE", ipAddress, "Échec d'authentification : mot de passe ou email incorrect.");
            throw e;
        }

        Utilisateur user = utilisateurRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("Utilisateur introuvable en base après authentification réussie : {}", request.email());
                    return new RuntimeException("Utilisateur introuvable");
                });

        // Enregistrer la date de connexion
        user.setDateDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(user);

        // Journal de sécurité
        journalSecuriteService.enregistrerEvenement(user, user.getEmail(), "CONNEXION_REUSSIE", ipAddress, "Connexion de l'utilisateur réussie.");

        String jwtToken = jwtService.generateToken(user);
        log.info("Token JWT généré avec succès pour : {}", request.email());

        UtilisateurDto utilisateurDto = new UtilisateurDto(
                user.getId().toString(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(jwtToken, utilisateurDto);
    }

    @Transactional
    public AuthResponse register(InscriptionRequest request, String ipAddress) {
        log.info("Inscription d'un nouveau propriétaire : {}", request.email());

        if (utilisateurRepository.findByEmail(request.email()).isPresent()) {
            journalSecuriteService.enregistrerEvenement(null, request.email(), "INSCRIPTION_ECHOUEE", ipAddress, "Tentative d'inscription avec un email déjà existant.");
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // 1. Créer le propriétaire
        Proprietaire proprietaire = Proprietaire.builder()
                .prenom(request.prenom())
                .nom(request.nom())
                .email(request.email())
                .build();
        proprietaire = proprietaireRepository.save(proprietaire);

        // 2. Créer l'utilisateur
        Utilisateur user = Utilisateur.builder()
                .email(request.email())
                .motDePasseHash(passwordEncoder.encode(request.motDePasse()))
                .prenom(request.prenom())
                .nom(request.nom())
                .role(Role.PROPRIETAIRE)
                .actif(true)
                .proprietaire(proprietaire)
                .build();
        user = utilisateurRepository.save(user);

        // 3. Créer le logement
        String nomLog = (request.nomLogement() != null && !request.nomLogement().isBlank()) ? request.nomLogement() : "Ma Tiny House";
        Logement logement = Logement.builder()
                .proprietaire(proprietaire)
                .nom(nomLog)
                .adresse(request.adresseLogement())
                .codePostal(request.codePostalLogement())
                .ville(request.villeLogement())
                .qualificationLogement(QualificationLogement.A_VERIFIER)
                .estDeplacable(true)
                .estSurTerrainResidencePrincipale(false)
                .estLoueCourteDuree(true)
                .estMeubleTourisme(false)
                .dateDebutLocation(LocalDate.now())
                .build();
        logementRepository.save(logement);

        // 4. Enregistrer l'événement de sécurité
        journalSecuriteService.enregistrerEvenement(user, user.getEmail(), "CONNEXION_REUSSIE", ipAddress, "Inscription et première connexion du propriétaire réussies.");

        String jwtToken = jwtService.generateToken(user);
        UtilisateurDto utilisateurDto = new UtilisateurDto(
                user.getId().toString(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(jwtToken, utilisateurDto);
    }
}
