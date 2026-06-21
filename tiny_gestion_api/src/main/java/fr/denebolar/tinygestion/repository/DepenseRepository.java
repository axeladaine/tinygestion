package fr.denebolar.tinygestion.repository;

import fr.denebolar.tinygestion.domain.Depense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DepenseRepository extends JpaRepository<Depense, Long> {
    List<Depense> findByLogementId(Long logementId);
    List<Depense> findByLogementIdAndDateDepenseBetween(Long logementId, LocalDate start, LocalDate end);
}
