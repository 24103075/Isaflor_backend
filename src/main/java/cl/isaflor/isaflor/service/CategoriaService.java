
package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.isaflor.isaflor.dto.CategoriaDTO;
import cl.isaflor.isaflor.model.Categoria;
import cl.isaflor.isaflor.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // Constructor
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Listar todas las categorias registradas
    public List<CategoriaDTO> listarCategorias() {

        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    // Convertir la entidad Categoria a CategoriaDTO
    private CategoriaDTO convertirADTO(Categoria categoria) {

        CategoriaDTO categoriaDTO = new CategoriaDTO();

        categoriaDTO.setIdCategoria(categoria.getIdCategoria());

        categoriaDTO.setNombreCategoria(categoria.getNombreCategoria());

        categoriaDTO.setDescripcion(categoria.getDescripcion());

        return categoriaDTO;
    }
}
