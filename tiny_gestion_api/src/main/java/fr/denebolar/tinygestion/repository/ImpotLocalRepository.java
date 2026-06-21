package fr.denebolar.tinygestion.repository;

import fr.denebolar.tinygestion.domain.ImpotLocal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImpotLocalRepository extends JpaRepository<ImpotLocal, Long> {
    List<ImpotLocal> findByLogementId(Long logementId);
}
