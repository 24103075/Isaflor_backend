package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.isaflor.isaflor.model.MetodoEnvio;

@Repository 
public interface MetodoEnvioRepository extends JpaRepository<MetodoEnvio, Integer>{

}