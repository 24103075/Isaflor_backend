package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.isaflor.isaflor.model.Comuna;

@Repository 
public interface ComunaRepository extends JpaRepository<Comuna, Integer> {

    boolean existsByRegion_IdRegion(Integer idRegion);
}
