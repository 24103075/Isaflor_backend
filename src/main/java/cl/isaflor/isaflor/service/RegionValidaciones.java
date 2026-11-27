package cl.isaflor.isaflor.service;

import org.springframework.stereotype.Service;
import cl.isaflor.isaflor.model.Region;

@Service
public class RegionValidaciones {

    public void validar(Region region) {
        if (region == null) {
            throw new IllegalArgumentException("La region es obligatoria");
        }

        if (region.getNombreRegion() == null
                || region.getNombreRegion().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la region es obligatorio");
        }

        int largoNombre = region.getNombreRegion().trim().length();
        if (largoNombre < 2 || largoNombre > 100) {
            throw new IllegalArgumentException(
                    "El nombre de la region debe tener entre 2 y 100 caracteres");
        }
    }
}
