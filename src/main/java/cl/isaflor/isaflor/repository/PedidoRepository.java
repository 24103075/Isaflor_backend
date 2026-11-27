package cl.isaflor.isaflor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.isaflor.isaflor.model.Pedido;

@Repository 
public interface PedidoRepository extends JpaRepository<Pedido, Integer>{

    boolean existsByMetodoEnvio_IdMetodoEnvio(Integer idMetodoEnvio);

    boolean existsByTipoDocumento_IdTipoDocumento(Integer idTipoDocumento);

    List<Pedido> findAllByOrderByFechaPedidoDesc();

    List<Pedido> findByUsuario_CorreoIgnoreCaseOrderByFechaPedidoDesc(String correo);
}