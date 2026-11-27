package cl.isaflor.isaflor.service;

import org.springframework.stereotype.Service;

import cl.isaflor.isaflor.model.Comuna;

@Service
public class ComunaValidaciones {

    public void validarComuna(Comuna comuna) {

        if (comuna == null) {
            throw new IllegalArgumentException("La comuna es obligatoria");
        }

        if (comuna.getNombreComuna() == null || comuna.getNombreComuna().isBlank()) {
            throw new IllegalArgumentException("El nombre de la comuna es obligatorio");
        }

        if (comuna.getNombreComuna().length() > 100) {
            throw new IllegalArgumentException("El nombre de la comuna no puede superar los 100 caracteres");
        }

        if (comuna.getRegion() == null || comuna.getRegion().getIdRegion() == null) {
            throw new IllegalArgumentException("Debe seleccionar una region");
        }
    }

}
