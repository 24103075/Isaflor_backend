
package cl.isaflor.isaflor.service;

import org.springframework.stereotype.Service;

import cl.isaflor.isaflor.model.Comuna;

@Service
public class ComunaValidaciones {

    public void validarComuna(Comuna comuna) {

        if (comuna == null) {
            throw new IllegalArgumentException(
                    "La comuna es obligatoria");
        }

        if (comuna.getNombreComuna() == null
                || comuna.getNombreComuna().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la comuna es obligatorio");
        }

        int largoNombre = comuna.getNombreComuna().trim().length();

        if (largoNombre < 2 || largoNombre > 100) {
            throw new IllegalArgumentException(
                    "El nombre de la comuna debe tener entre 2 y 100 caracteres");
        }

        if (comuna.getRegion() == null
                || comuna.getRegion().getIdRegion() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una region");
        }
    }
}
