package fr.denebolar.tinygestion.repository;

import fr.denebolar.tinygestion.domain.JournalSecurite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalSecuriteRepository extends JpaRepository<JournalSecurite, Long> {
    List<JournalSecurite> findAllByOrderByDateEvenementDesc();
}
