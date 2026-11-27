package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.TipoDocumentoDTO;
import cl.isaflor.isaflor.model.TipoDocumento;
import cl.isaflor.isaflor.repository.PedidoRepository;
import cl.isaflor.isaflor.repository.TipoDocumentoRepository;

@Service
@Transactional
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PedidoRepository pedidoRepository;
    private final TipoDocumentoValidaciones tipoDocumentoValidaciones;

    public TipoDocumentoService(
            TipoDocumentoRepository tipoDocumentoRepository,
            PedidoRepository pedidoRepository,
            TipoDocumentoValidaciones tipoDocumentoValidaciones) {
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.pedidoRepository = pedidoRepository;
        this.tipoDocumentoValidaciones = tipoDocumentoValidaciones;
    }

    @Transactional(readOnly = true)
    public List<TipoDocumentoDTO> listarTiposDocumento() {
        return tipoDocumentoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TipoDocumentoDTO buscarPorId(Integer id) {
        return convertirADTO(buscarTipoDocumentoEntidad(id));
    }

    public TipoDocumentoDTO guardarTipoDocumento(TipoDocumento datos) {
        tipoDocumentoValidaciones.validar(datos);

        String nombre = datos.getNombreDocumento().trim();
        if (tipoDocumentoRepository.existsByNombreDocumentoIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un tipo de documento con ese nombre");
        }

        TipoDocumento nuevo = new TipoDocumento();
        nuevo.setNombreDocumento(nombre);
        nuevo.setDescripcion(normalizarDescripcion(datos.getDescripcion()));

        return convertirADTO(tipoDocumentoRepository.save(nuevo));
    }

    public TipoDocumentoDTO actualizarTipoDocumento(
            Integer id,
            TipoDocumento datos) {
        TipoDocumento existente = buscarTipoDocumentoEntidad(id);
        tipoDocumentoValidaciones.validar(datos);

        String nombre = datos.getNombreDocumento().trim();
        if (tipoDocumentoRepository
                .existsByNombreDocumentoIgnoreCaseAndIdTipoDocumentoNot(nombre, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otro tipo de documento con ese nombre");
        }

        existente.setNombreDocumento(nombre);
        existente.setDescripcion(normalizarDescripcion(datos.getDescripcion()));

        return convertirADTO(tipoDocumentoRepository.save(existente));
    }

    public String eliminarTipoDocumento(Integer id) {
        TipoDocumento tipoDocumento = buscarTipoDocumentoEntidad(id);

        if (pedidoRepository.existsByTipoDocumento_IdTipoDocumento(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el tipo de documento porque tiene pedidos asociados");
        }

        tipoDocumentoRepository.delete(tipoDocumento);
        return "Tipo de documento eliminado correctamente";
    }

    private TipoDocumento buscarTipoDocumentoEntidad(Integer id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del tipo de documento debe ser valido");
        }

        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro el tipo de documento con ID: " + id));
    }

    private String normalizarDescripcion(String descripcion) {
        return descripcion == null || descripcion.isBlank()
                ? null
                : descripcion.trim();
    }

    private TipoDocumentoDTO convertirADTO(TipoDocumento tipoDocumento) {
        TipoDocumentoDTO dto = new TipoDocumentoDTO();
        dto.setIdTipoDocumento(tipoDocumento.getIdTipoDocumento());
        dto.setNombreDocumento(tipoDocumento.getNombreDocumento());
        dto.setDescripcion(tipoDocumento.getDescripcion());
        return dto;
    }
}
