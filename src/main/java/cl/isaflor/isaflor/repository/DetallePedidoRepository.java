package cl.isaflor.isaflor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.isaflor.isaflor.model.DetallePedido;

@Repository 
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer>{

    List<DetallePedido> findByPedido_IdPedido(Integer idPedido);

    List<DetallePedido> findByPedido_Usuario_CorreoIgnoreCase(String correo);
}