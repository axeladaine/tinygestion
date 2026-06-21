package fr.denebolar.tinygestion.repository;

import fr.denebolar.tinygestion.domain.Logement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogementRepository extends JpaRepository<Logement, Long> {
    List<Logement> findByProprietaireId(Long proprietaireId);
}
