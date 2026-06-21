package fr.denebolar.tinygestion.repository;

import fr.denebolar.tinygestion.domain.Recette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecetteRepository extends JpaRepository<Recette, Long> {
    List<Recette> findByLogementId(Long logementId);
    List<Recette> findByLogementIdAndDateEncaissementBetween(Long logementId, LocalDate start, LocalDate end);
}
