package fr.denebolar.tinygestion.repository;

import fr.denebolar.tinygestion.domain.BienAmortissable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BienAmortissableRepository extends JpaRepository<BienAmortissable, Long> {
    List<BienAmortissable> findByLogementId(Long logementId);
}
