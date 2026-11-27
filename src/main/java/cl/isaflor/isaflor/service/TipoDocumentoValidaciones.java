package cl.isaflor.isaflor.service;

import org.springframework.stereotype.Service;

import cl.isaflor.isaflor.model.TipoDocumento;

@Service
public class TipoDocumentoValidaciones {

    public void validar(TipoDocumento tipoDocumento) {
        if (tipoDocumento == null) {
            throw new IllegalArgumentException(
                    "El tipo de documento es obligatorio");
        }

        if (tipoDocumento.getNombreDocumento() == null
                || tipoDocumento.getNombreDocumento().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del documento es obligatorio");
        }

        int largoNombre = tipoDocumento.getNombreDocumento().trim().length();
        if (largoNombre < 2 || largoNombre > 50) {
            throw new IllegalArgumentException(
                    "El nombre del documento debe tener entre 2 y 50 caracteres");
        }

        if (tipoDocumento.getDescripcion() != null
                && tipoDocumento.getDescripcion().trim().length() > 150) {
            throw new IllegalArgumentException(
                    "La descripcion no puede superar los 150 caracteres");
        }
    }
}
