package fr.denebolar.tinygestion.repository;

import fr.denebolar.tinygestion.domain.DocumentJustificatif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentJustificatifRepository extends JpaRepository<DocumentJustificatif, Long> {
    List<DocumentJustificatif> findByLogementId(Long logementId);
}
