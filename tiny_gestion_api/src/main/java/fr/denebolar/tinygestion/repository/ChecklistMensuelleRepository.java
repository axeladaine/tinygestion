package fr.denebolar.tinygestion.repository;

import fr.denebolar.tinygestion.domain.ChecklistMensuelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistMensuelleRepository extends JpaRepository<ChecklistMensuelle, Long> {
    List<ChecklistMensuelle> findByLogementIdAndAnnee(Long logementId, Integer annee);
    Optional<ChecklistMensuelle> findByLogementIdAndAnneeAndMois(Long logementId, Integer annee, Integer mois);
}
