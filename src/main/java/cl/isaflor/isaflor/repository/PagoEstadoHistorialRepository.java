package cl.isaflor.isaflor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.isaflor.isaflor.model.PagoEstadoHistorial;

@Repository
public interface PagoEstadoHistorialRepository
        extends JpaRepository<PagoEstadoHistorial, Integer> {

    List<PagoEstadoHistorial> findByPago_IdPagoOrderByFechaCambioAsc(Integer idPago);
}
