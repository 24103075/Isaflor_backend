package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.model.MetodoPago;
import cl.isaflor.isaflor.repository.MetodoPagoRepository;
import cl.isaflor.isaflor.repository.PagoRepository;

@Service
@Transactional
public class MetodoPagoService {

    private final MetodoPagoRepository metodoPagoRepository;
    private final PagoRepository pagoRepository;
    private final MetodoPagoValidaciones metodoPagoValidaciones;

    public MetodoPagoService(
            MetodoPagoRepository metodoPagoRepository,
            PagoRepository pagoRepository,
            MetodoPagoValidaciones metodoPagoValidaciones) {
        this.metodoPagoRepository = metodoPagoRepository;
        this.pagoRepository = pagoRepository;
        this.metodoPagoValidaciones = metodoPagoValidaciones;
    }

    @Transactional(readOnly = true)
    public List<MetodoPago> listarMetodosPago(boolean administrador) {
        return administrador
                ? metodoPagoRepository.findAll()
                : metodoPagoRepository.findByEstadoTrue();
    }

    @Transactional(readOnly = true)
    public MetodoPago buscarPorId(Integer id, boolean administrador) {
        MetodoPago metodoPago = buscarMetodoPagoEntidad(id);
        if (!administrador && !Boolean.TRUE.equals(metodoPago.getEstado())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Metodo de pago no encontrado");
        }
        return metodoPago;
    }

    public MetodoPago guardarMetodoPago(MetodoPago metodoPago) {
        metodoPagoValidaciones.validar(metodoPago);
        normalizar(metodoPago);
        return metodoPagoRepository.save(metodoPago);
    }

    public MetodoPago actualizarMetodoPago(Integer id, MetodoPago datos) {
        MetodoPago existente = buscarMetodoPagoEntidad(id);
        metodoPagoValidaciones.validar(datos);

        existente.setNombre(datos.getNombre().trim());
        existente.setDescripcion(normalizarDescripcion(datos.getDescripcion()));
        existente.setEstado(datos.getEstado());

        return metodoPagoRepository.save(existente);
    }

    public String eliminarMetodoPago(Integer id) {
        MetodoPago metodoPago = buscarMetodoPagoEntidad(id);
        if (pagoRepository.existsByMetodoPago_IdMetodoPago(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el metodo de pago porque esta asociado a pagos");
        }

        metodoPagoRepository.delete(metodoPago);
        return "Metodo de pago eliminado correctamente";
    }

    private MetodoPago buscarMetodoPagoEntidad(Integer id) {
        return metodoPagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el metodo de pago con ID: " + id));
    }

    private void normalizar(MetodoPago metodoPago) {
        metodoPago.setNombre(metodoPago.getNombre().trim());
        metodoPago.setDescripcion(normalizarDescripcion(metodoPago.getDescripcion()));
    }

    private String normalizarDescripcion(String descripcion) {
        return descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
    }
}
