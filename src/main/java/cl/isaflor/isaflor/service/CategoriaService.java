
package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.CategoriaDTO;
import cl.isaflor.isaflor.model.Categoria;
import cl.isaflor.isaflor.repository.CategoriaRepository;
import cl.isaflor.isaflor.repository.ProductoRepository;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaValidaciones categoriaValidaciones;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            CategoriaValidaciones categoriaValidaciones) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.categoriaValidaciones = categoriaValidaciones;
    }

    // LISTAR CATEGORIAS
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // BUSCAR CATEGORIA POR ID
    @Transactional(readOnly = true)
    public CategoriaDTO buscarPorId(Integer id) {
        return convertirADTO(buscarCategoriaEntidad(id));
    }

    // GUARDAR CATEGORIA
    public CategoriaDTO guardarCategoria(Categoria datos) {
        categoriaValidaciones.validar(datos);

        String nombre = datos.getNombreCategoria().trim();

        if (categoriaRepository.existsByNombreCategoriaIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una categoria con ese nombre");
        }

        Categoria nueva = new Categoria();
        nueva.setNombreCategoria(nombre);
        nueva.setDescripcion(normalizarDescripcion(datos.getDescripcion()));

        return convertirADTO(categoriaRepository.save(nueva));
    }

    // ACTUALIZAR CATEGORIA
    public CategoriaDTO actualizarCategoria(Integer id, Categoria datos) {
        Categoria existente = buscarCategoriaEntidad(id);

        categoriaValidaciones.validar(datos);

        String nombre = datos.getNombreCategoria().trim();

        if (categoriaRepository.existsByNombreCategoriaIgnoreCaseAndIdCategoriaNot(
                nombre, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otra categoria con ese nombre");
        }

        existente.setNombreCategoria(nombre);
        existente.setDescripcion(normalizarDescripcion(datos.getDescripcion()));

        return convertirADTO(categoriaRepository.save(existente));
    }

    // ELIMINAR CATEGORIA
    public String eliminarCategoria(Integer id) {
        Categoria categoria = buscarCategoriaEntidad(id);

        if (productoRepository.existsByCategoria_IdCategoria(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar la categoria porque tiene productos asociados");
        }

        categoriaRepository.delete(categoria);

        return "Categoria eliminada correctamente";
    }

    // BUSCAR ENTIDAD POR ID
    private Categoria buscarCategoriaEntidad(Integer id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID de la categoria debe ser valido");
        }

        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro la categoria con ID: " + id));
    }

    // NORMALIZAR DESCRIPCION
    private String normalizarDescripcion(String descripcion) {
        return descripcion == null || descripcion.isBlank()
                ? null
                : descripcion.trim();
    }

    // CONVERTIR ENTIDAD A DTO
    private CategoriaDTO convertirADTO(Categoria categoria) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setIdCategoria(categoria.getIdCategoria());
        dto.setNombreCategoria(categoria.getNombreCategoria());
        dto.setDescripcion(categoria.getDescripcion());

        return dto;
    }
}
