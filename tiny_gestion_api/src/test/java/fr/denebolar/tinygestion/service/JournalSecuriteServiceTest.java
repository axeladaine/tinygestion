package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.JournalSecurite;
import fr.denebolar.tinygestion.domain.Role;
import fr.denebolar.tinygestion.domain.Utilisateur;
import fr.denebolar.tinygestion.repository.JournalSecuriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JournalSecuriteServiceTest {

    @Mock
    private JournalSecuriteRepository repository;

    @InjectMocks
    private JournalSecuriteService service;

    private Utilisateur owner;
    private Utilisateur assistant;

    @BeforeEach
    public void setUp() {
        owner = Utilisateur.builder().id(10L).email("owner@example.com").role(Role.PROPRIETAIRE).build();
        assistant = Utilisateur.builder().id(11L).email("assistant@example.com").role(Role.ASSISTANT).build();
    }

    @Test
    public void should_log_event_with_user_and_explicit_email() {
        service.enregistrerEvenement(owner, "explicit@example.com", "TEST_EVENT", "127.0.0.1", "Some details");

        ArgumentCaptor<JournalSecurite> captor = ArgumentCaptor.forClass(JournalSecurite.class);
        verify(repository, times(1)).save(captor.capture());

        JournalSecurite saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(owner, saved.getUtilisateur());
        assertEquals("explicit@example.com", saved.getEmail());
        assertEquals("TEST_EVENT", saved.getTypeEvenement());
        assertEquals("127.0.0.1", saved.getAdresseIp());
        assertEquals("Some details", saved.getDetails());
    }

    @Test
    public void should_log_event_with_user_only_email_resolving_to_user_email() {
        service.enregistrerEvenement(owner, null, "TEST_EVENT", "127.0.0.1", "Some details");

        ArgumentCaptor<JournalSecurite> captor = ArgumentCaptor.forClass(JournalSecurite.class);
        verify(repository, times(1)).save(captor.capture());

        JournalSecurite saved = captor.getValue();
        assertNotNull(saved);
        assertEquals("owner@example.com", saved.getEmail());
    }

    @Test
    public void should_log_event_with_no_user_and_no_email() {
        service.enregistrerEvenement(null, null, "TEST_EVENT", "127.0.0.1", "Some details");

        ArgumentCaptor<JournalSecurite> captor = ArgumentCaptor.forClass(JournalSecurite.class);
        verify(repository, times(1)).save(captor.capture());

        JournalSecurite saved = captor.getValue();
        assertNotNull(saved);
        assertNull(saved.getUtilisateur());
        assertNull(saved.getEmail());
    }

    @Test
    public void should_return_all_events_for_owner() {
        JournalSecurite event = JournalSecurite.builder().id(100L).typeEvenement("TEST").build();
        when(repository.findAllByOrderByDateEvenementDesc()).thenReturn(Collections.singletonList(event));

        List<JournalSecurite> result = service.getTousLesEvenements(owner);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
    }

    @Test
    public void should_throw_exception_when_getTousLesEvenements_by_non_owner() {
        assertThrows(RuntimeException.class, () -> {
            service.getTousLesEvenements(assistant);
        });
    }
}
