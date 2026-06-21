package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.Proprietaire;
import fr.denebolar.tinygestion.domain.Role;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.dto.auth.CreerAssistantRequest;
import fr.denebolar.tinygestion.dto.auth.UtilisateurDto;
import fr.denebolar.tinygestion.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdministrationServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JournalSecuriteService journalSecuriteService;

    @InjectMocks
    private AdministrationService service;

    private Utilisateur owner;
    private Proprietaire proprietaire;
    private Utilisateur assistant;
    private CreerAssistantRequest request;

    @BeforeEach
    public void setUp() {
        proprietaire = Proprietaire.builder().id(1L).nom("Owner").build();
        owner = Utilisateur.builder()
                .id(10L)
                .email("owner@example.com")
                .role(Role.PROPRIETAIRE)
                .proprietaire(proprietaire)
                .build();

        assistant = Utilisateur.builder()
                .id(11L)
                .email("assistant@example.com")
                .nom("AssistantNom")
                .prenom("AssistantPrenom")
                .role(Role.ASSISTANT)
                .proprietaire(proprietaire)
                .actif(true)
                .build();

        request = new CreerAssistantRequest("assistant@example.com", "password123", "AssistantPrenom", "AssistantNom");
    }

    @Test
    public void should_create_assistant_when_owner_is_valid() {
        when(utilisateurRepository.findByEmail("assistant@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(assistant);

        UtilisateurDto result = service.creerAssistant(request, owner, "127.0.0.1");

        assertNotNull(result);
        assertEquals("assistant@example.com", result.email());
        assertEquals("AssistantNom", result.nom());
        assertEquals("AssistantPrenom", result.prenom());
        assertEquals("ASSISTANT", result.role());

        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
        verify(journalSecuriteService, times(1)).enregistrerEvenement(
                eq(owner), eq("owner@example.com"), eq("AJOUT_ASSISTANT"), eq("127.0.0.1"), anyString()
        );
    }

    @Test
    public void should_throw_exception_when_creerAssistant_by_non_owner() {
        Utilisateur nonOwner = Utilisateur.builder().role(Role.ASSISTANT).build();

        assertThrows(RuntimeException.class, () -> {
            service.creerAssistant(request, nonOwner, "127.0.0.1");
        });
    }

    @Test
    public void should_throw_exception_when_owner_has_no_proprietaire_record() {
        Utilisateur ownerWithoutProprio = Utilisateur.builder().role(Role.PROPRIETAIRE).proprietaire(null).build();

        assertThrows(RuntimeException.class, () -> {
            service.creerAssistant(request, ownerWithoutProprio, "127.0.0.1");
        });
    }

    @Test
    public void should_throw_exception_when_email_already_exists() {
        when(utilisateurRepository.findByEmail("assistant@example.com")).thenReturn(Optional.of(assistant));

        assertThrows(RuntimeException.class, () -> {
            service.creerAssistant(request, owner, "127.0.0.1");
        });
    }

    @Test
    public void should_return_assistants_list_for_owner() {
        when(utilisateurRepository.findByProprietaireId(1L)).thenReturn(Collections.singletonList(assistant));

        List<UtilisateurDto> result = service.getAssistants(owner);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("assistant@example.com", result.get(0).email());
    }

    @Test
    public void should_throw_exception_when_getAssistants_by_non_owner() {
        Utilisateur nonOwner = Utilisateur.builder().role(Role.ASSISTANT).build();

        assertThrows(RuntimeException.class, () -> {
            service.getAssistants(nonOwner);
        });
    }

    @Test
    public void should_throw_exception_when_getAssistants_owner_has_no_proprietaire() {
        Utilisateur ownerWithoutProprio = Utilisateur.builder().role(Role.PROPRIETAIRE).proprietaire(null).build();

        assertThrows(RuntimeException.class, () -> {
            service.getAssistants(ownerWithoutProprio);
        });
    }

    @Test
    public void should_delete_assistant_when_owner_is_valid() {
        when(utilisateurRepository.findById(11L)).thenReturn(Optional.of(assistant));

        service.supprimerAssistant(11L, owner, "127.0.0.1");

        verify(utilisateurRepository, times(1)).delete(assistant);
        verify(journalSecuriteService, times(1)).enregistrerEvenement(
                eq(owner), eq("owner@example.com"), eq("SUPPRESSION_ASSISTANT"), eq("127.0.0.1"), anyString()
        );
    }

    @Test
    public void should_throw_exception_when_delete_by_non_owner() {
        Utilisateur nonOwner = Utilisateur.builder().role(Role.ASSISTANT).build();

        assertThrows(RuntimeException.class, () -> {
            service.supprimerAssistant(11L, nonOwner, "127.0.0.1");
        });
    }

    @Test
    public void should_throw_exception_when_delete_owner_has_no_proprietaire() {
        Utilisateur ownerWithoutProprio = Utilisateur.builder().role(Role.PROPRIETAIRE).proprietaire(null).build();

        assertThrows(RuntimeException.class, () -> {
            service.supprimerAssistant(11L, ownerWithoutProprio, "127.0.0.1");
        });
    }

    @Test
    public void should_throw_exception_when_delete_assistant_not_found() {
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.supprimerAssistant(99L, owner, "127.0.0.1");
        });
    }

    @Test
    public void should_throw_exception_when_delete_assistant_belongs_to_other_owner() {
        Proprietaire otherProprio = Proprietaire.builder().id(2L).build();
        Utilisateur otherAssistant = Utilisateur.builder()
                .id(12L)
                .email("other@example.com")
                .proprietaire(otherProprio)
                .build();

        when(utilisateurRepository.findById(12L)).thenReturn(Optional.of(otherAssistant));

        assertThrows(RuntimeException.class, () -> {
            service.supprimerAssistant(12L, owner, "127.0.0.1");
        });
    }
}
