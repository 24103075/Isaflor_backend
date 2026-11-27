package cl.isaflor.isaflor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.isaflor.isaflor.model.Pago;

@Repository 
public interface PagoRepository extends JpaRepository<Pago, Integer>{

    boolean existsByMetodoPago_IdMetodoPago(Integer idMetodoPago);

    boolean existsByPedido_IdPedido(Integer idPedido);

    boolean existsByPedido_IdPedidoAndEstadoPago(Integer idPedido, String estadoPago);

    List<Pago> findAllByOrderByFechaPagoDesc();

    List<Pago> findByPedido_Usuario_CorreoIgnoreCaseOrderByFechaPagoDesc(
            String correoUsuario);
}
