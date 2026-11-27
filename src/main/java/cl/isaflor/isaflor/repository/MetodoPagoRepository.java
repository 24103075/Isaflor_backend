package cl.isaflor.isaflor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.isaflor.isaflor.model.MetodoPago;

@Repository 
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer>{

    List<MetodoPago> findByEstadoTrue();
}
