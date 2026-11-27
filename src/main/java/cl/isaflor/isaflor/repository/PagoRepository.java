package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.isaflor.isaflor.model.Pago;

@Repository 
public interface PagoRepository extends JpaRepository<Pago, Integer>{

    boolean existsByMetodoPago_IdMetodoPago(Integer idMetodoPago);
}
