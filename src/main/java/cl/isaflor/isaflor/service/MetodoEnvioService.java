package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.model.MetodoEnvio;
import cl.isaflor.isaflor.repository.MetodoEnvioRepository;
import cl.isaflor.isaflor.repository.PedidoRepository;

@Service
@Transactional
public class MetodoEnvioService {

    private final MetodoEnvioRepository metodoEnvioRepository;
    private final PedidoRepository pedidoRepository;
    private final MetodoEnvioValidaciones metodoEnvioValidaciones;

    public MetodoEnvioService(
            MetodoEnvioRepository metodoEnvioRepository,
            PedidoRepository pedidoRepository,
            MetodoEnvioValidaciones metodoEnvioValidaciones) {
        this.metodoEnvioRepository = metodoEnvioRepository;
        this.pedidoRepository = pedidoRepository;
        this.metodoEnvioValidaciones = metodoEnvioValidaciones;
    }

    @Transactional(readOnly = true)
    public List<MetodoEnvio> listarMetodosEnvio() {
        return metodoEnvioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MetodoEnvio buscarPorId(Integer id) {
        return buscarMetodoEnvioEntidad(id);
    }

    public MetodoEnvio guardarMetodoEnvio(MetodoEnvio metodoEnvio) {
        metodoEnvioValidaciones.validar(metodoEnvio);
        normalizar(metodoEnvio);
        return metodoEnvioRepository.save(metodoEnvio);
    }

    public MetodoEnvio actualizarMetodoEnvio(Integer id, MetodoEnvio datos) {
        MetodoEnvio existente = buscarMetodoEnvioEntidad(id);
        metodoEnvioValidaciones.validar(datos);

        existente.setNombre(datos.getNombre().trim());
        existente.setDescripcion(normalizarDescripcion(datos.getDescripcion()));
        existente.setCosto(datos.getCosto());
        existente.setTiempoEntrega(datos.getTiempoEntrega().trim());

        return metodoEnvioRepository.save(existente);
    }

    public String eliminarMetodoEnvio(Integer id) {
        MetodoEnvio metodoEnvio = buscarMetodoEnvioEntidad(id);

        if (pedidoRepository.existsByMetodoEnvio_IdMetodoEnvio(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el metodo de envio porque esta asociado a pedidos");
        }

        metodoEnvioRepository.delete(metodoEnvio);
        return "Metodo de envio eliminado correctamente";
    }

    private MetodoEnvio buscarMetodoEnvioEntidad(Integer id) {
        return metodoEnvioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el metodo de envio con ID: " + id));
    }

    private void normalizar(MetodoEnvio metodoEnvio) {
        metodoEnvio.setNombre(metodoEnvio.getNombre().trim());
        metodoEnvio.setDescripcion(normalizarDescripcion(metodoEnvio.getDescripcion()));
        metodoEnvio.setTiempoEntrega(metodoEnvio.getTiempoEntrega().trim());
    }

    private String normalizarDescripcion(String descripcion) {
        return descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
    }
}
