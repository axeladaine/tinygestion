package fr.denebolar.tinygestion.service;

import fr.denebolar.tinygestion.domain.*;
import fr.denebolar.tinygestion.repository.DepenseRepository;
import fr.denebolar.tinygestion.repository.DocumentJustificatifRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepenseServiceTest {

    @Mock
    private DepenseRepository repository;

    @Mock
    private LogementService logementService;

    @Mock
    private DocumentJustificatifRepository documentRepository;

    @InjectMocks
    private DepenseService service;

    private Utilisateur user;
    private Logement logement;
    private Depense depense;

    @BeforeEach
    public void setUp() {
        user = Utilisateur.builder().id(1L).email("test@example.com").role(Role.PROPRIETAIRE).build();
        logement = Logement.builder().id(10L).nom("Tiny test").build();
        depense = Depense.builder().id(200L).logement(logement).montantTtc(BigDecimal.valueOf(150)).build();
    }

    @Test
    public void should_return_depenses_for_logement() {
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.findByLogementId(10L)).thenReturn(Collections.singletonList(depense));

        List<Depense> result = service.getDepensesByLogement(10L, user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(200L, result.get(0).getId());
    }

    @Test
    public void should_return_depenses_for_logement_and_period() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.findByLogementIdAndDateDepenseBetween(10L, start, end))
                .thenReturn(Collections.singletonList(depense));

        List<Depense> result = service.getDepensesByLogementAndPeriod(10L, start, end, user);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByLogementIdAndDateDepenseBetween(10L, start, end);
    }

    @Test
    public void should_save_new_depense() {
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.save(any(Depense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Depense result = service.saveDepense(depense, user);

        assertNotNull(result);
        assertEquals(logement, result.getLogement());
        verify(repository, times(1)).save(depense);
    }

    @Test
    public void should_update_existing_depense() {
        Depense details = Depense.builder()
                .fournisseur("EDF")
                .montantTtc(BigDecimal.valueOf(180))
                .statutDeductibilite(StatutDeductibilite.DEDUCTIBLE)
                .build();

        when(repository.findById(200L)).thenReturn(Optional.of(depense));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.save(any(Depense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Depense result = service.updateDepense(200L, details, user);

        assertNotNull(result);
        assertEquals("EDF", result.getFournisseur());
        assertEquals(0, BigDecimal.valueOf(180).compareTo(result.getMontantTtc()));
        assertEquals(StatutDeductibilite.DEDUCTIBLE, result.getStatutDeductibilite());
        verify(repository, times(1)).save(depense);
    }

    @Test
    public void should_update_existing_depense_with_different_logement() {
        Logement otherLogement = Logement.builder().id(20L).nom("Autre Tiny").build();
        Depense details = Depense.builder()
                .logement(otherLogement)
                .fournisseur("EDF")
                .build();

        when(repository.findById(200L)).thenReturn(Optional.of(depense));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(logementService.getLogementById(20L, user)).thenReturn(otherLogement);
        when(repository.save(any(Depense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Depense result = service.updateDepense(200L, details, user);

        assertNotNull(result);
        assertEquals(otherLogement, result.getLogement());
        verify(repository, times(1)).save(depense);
    }

    @Test
    public void should_delete_existing_depense() {
        when(repository.findById(200L)).thenReturn(Optional.of(depense));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);

        service.deleteDepense(200L, user);

        verify(repository, times(1)).delete(depense);
    }

    @Test
    public void should_save_depense_with_justificatif() {
        DocumentJustificatif doc = DocumentJustificatif.builder().id(300L).build();
        depense.setDocumentJustificatif(doc);

        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.save(any(Depense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentRepository.findById(300L)).thenReturn(Optional.of(doc));

        Depense result = service.saveDepense(depense, user);

        assertNotNull(result);
        assertEquals(doc, result.getDocumentJustificatif());
        verify(documentRepository, times(1)).save(doc);
        assertEquals("DEPENSE", doc.getEntiteLieeType());
        assertEquals(200L, doc.getEntiteLieeId());
    }

    @Test
    public void should_update_depense_with_new_justificatif() {
        DocumentJustificatif oldDoc = DocumentJustificatif.builder().id(300L).entiteLieeType("DEPENSE").entiteLieeId(200L).build();
        depense.setDocumentJustificatif(oldDoc);

        DocumentJustificatif newDoc = DocumentJustificatif.builder().id(400L).build();
        Depense details = Depense.builder().documentJustificatif(newDoc).build();

        when(repository.findById(200L)).thenReturn(Optional.of(depense));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(documentRepository.findById(400L)).thenReturn(Optional.of(newDoc));
        when(repository.save(any(Depense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Depense result = service.updateDepense(200L, details, user);

        assertNotNull(result);
        assertEquals(newDoc, result.getDocumentJustificatif());
        verify(documentRepository, times(1)).save(oldDoc);
        verify(documentRepository, times(1)).save(newDoc);
        assertNull(oldDoc.getEntiteLieeType());
        assertEquals("DEPENSE", newDoc.getEntiteLieeType());
    }

    @Test
    public void should_update_depense_removing_justificatif() {
        DocumentJustificatif oldDoc = DocumentJustificatif.builder().id(300L).entiteLieeType("DEPENSE").entiteLieeId(200L).build();
        depense.setDocumentJustificatif(oldDoc);

        Depense details = Depense.builder().documentJustificatif(null).build();

        when(repository.findById(200L)).thenReturn(Optional.of(depense));
        when(logementService.getLogementById(10L, user)).thenReturn(logement);
        when(repository.save(any(Depense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Depense result = service.updateDepense(200L, details, user);

        assertNotNull(result);
        assertNull(result.getDocumentJustificatif());
        verify(documentRepository, times(1)).save(oldDoc);
        assertNull(oldDoc.getEntiteLieeType());
    }
}
