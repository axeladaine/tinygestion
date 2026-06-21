package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.dto.auth.*;
import fr.denebolar.tinygestion.repository.LogementRepository;
import fr.denebolar.tinygestion.repository.ProprietaireRepository;
import fr.denebolar.tinygestion.repository.UtilisateurRepository;
import fr.denebolar.tinygestion.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ProprietaireRepository proprietaireRepository;

    @Mock
    private LogementRepository logementRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JournalSecuriteService journalSecuriteService;

    @InjectMocks
    private AuthService authService;

    private Utilisateur user;
    private Proprietaire proprietaire;
    private AuthRequest authRequest;
    private InscriptionRequest inscriptionRequest;

    @BeforeEach
    public void setUp() {
        proprietaire = Proprietaire.builder().id(2L).prenom("Jean").nom("Dupont").email("jean@example.com").build();
        user = Utilisateur.builder()
                .id(10L)
                .email("jean@example.com")
                .nom("Dupont")
                .prenom("Jean")
                .role(Role.PROPRIETAIRE)
                .proprietaire(proprietaire)
                .actif(true)
                .build();

        authRequest = new AuthRequest("jean@example.com", "password123");
        inscriptionRequest = new InscriptionRequest(
                "jean@example.com",
                "password123",
                "Jean",
                "Dupont",
                "Ma Tiny Cabane",
                "10 Rue des bois",
                "33000",
                "Bordeaux"
        );
    }

    @Test
    public void should_login_successfully_when_credentials_are_valid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(utilisateurRepository.findByEmail("jean@example.com")).thenReturn(Optional.of(user));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwtTokenValue");

        AuthResponse response = authService.login(authRequest, "127.0.0.1");

        assertNotNull(response);
        assertEquals("jwtTokenValue", response.token());
        assertEquals("jean@example.com", response.utilisateur().email());
        assertEquals("PROPRIETAIRE", response.utilisateur().role());

        verify(utilisateurRepository, times(1)).save(user);
        verify(journalSecuriteService, times(1)).enregistrerEvenement(
                eq(user), eq("jean@example.com"), eq("CONNEXION_REUSSIE"), eq("127.0.0.1"), anyString()
        );
    }

    @Test
    public void should_throw_exception_when_login_fails() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authService.login(authRequest, "127.0.0.1");
        });

        verify(journalSecuriteService, times(1)).enregistrerEvenement(
                isNull(), eq("jean@example.com"), eq("CONNEXION_ECHOUEE"), eq("127.0.0.1"), anyString()
        );
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    public void should_throw_exception_when_user_not_found_after_auth() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(utilisateurRepository.findByEmail("jean@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            authService.login(authRequest, "127.0.0.1");
        });
    }

    @Test
    public void should_register_successfully_when_email_not_taken() {
        when(utilisateurRepository.findByEmail("jean@example.com")).thenReturn(Optional.empty());
        when(proprietaireRepository.save(any(Proprietaire.class))).thenReturn(proprietaire);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(user);
        when(logementRepository.save(any(Logement.class))).thenReturn(new Logement());
        when(jwtService.generateToken(user)).thenReturn("jwtTokenValue");

        AuthResponse response = authService.register(inscriptionRequest, "127.0.0.1");

        assertNotNull(response);
        assertEquals("jwtTokenValue", response.token());
        assertEquals("jean@example.com", response.utilisateur().email());

        verify(proprietaireRepository, times(1)).save(any(Proprietaire.class));
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
        verify(logementRepository, times(1)).save(any(Logement.class));
        verify(journalSecuriteService, times(1)).enregistrerEvenement(
                eq(user), eq("jean@example.com"), eq("CONNEXION_REUSSIE"), eq("127.0.0.1"), anyString()
        );
    }

    @Test
    public void should_throw_exception_when_registering_with_taken_email() {
        when(utilisateurRepository.findByEmail("jean@example.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> {
            authService.register(inscriptionRequest, "127.0.0.1");
        });

        verify(journalSecuriteService, times(1)).enregistrerEvenement(
                isNull(), eq("jean@example.com"), eq("INSCRIPTION_ECHOUEE"), eq("127.0.0.1"), anyString()
        );
        verify(proprietaireRepository, never()).save(any(Proprietaire.class));
    }
}
