package cl.isaflor.isaflor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.isaflor.isaflor.model.DetallePedido;

@Repository 
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer>{

    
}