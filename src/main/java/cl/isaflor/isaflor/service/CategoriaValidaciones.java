
package cl.isaflor.isaflor.service;

import org.springframework.stereotype.Service;

import cl.isaflor.isaflor.model.Categoria;

@Service
public class CategoriaValidaciones {

    // VALIDAR DATOS DE LA CATEGORIA
    public void validar(Categoria categoria) {

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "La categoria es obligatoria");
        }

        if (categoria.getNombreCategoria() == null
                || categoria.getNombreCategoria().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoria es obligatorio");
        }

        int largoNombre = categoria.getNombreCategoria().trim().length();

        if (largoNombre < 2 || largoNombre > 100) {
            throw new IllegalArgumentException(
                    "El nombre de la categoria debe tener entre 2 y 100 caracteres");
        }

        if (categoria.getDescripcion() != null
                && categoria.getDescripcion().trim().length() > 255) {
            throw new IllegalArgumentException(
                    "La descripcion no puede superar los 255 caracteres");
        }
    }
}
