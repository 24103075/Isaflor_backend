
package cl.isaflor.isaflor.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cl.isaflor.isaflor.model.Comuna;
import cl.isaflor.isaflor.model.Region;
import cl.isaflor.isaflor.repository.ComunaRepository;
import cl.isaflor.isaflor.repository.RegionRepository;

@Service
@Transactional
public class ComunaService {

    private final ComunaRepository comunaRepository;
    private final RegionRepository regionRepository;
    private final ComunaValidaciones comunaValidaciones;

    public ComunaService(
            ComunaRepository comunaRepository,
            RegionRepository regionRepository,
            ComunaValidaciones comunaValidaciones) {

        this.comunaRepository = comunaRepository;
        this.regionRepository = regionRepository;
        this.comunaValidaciones = comunaValidaciones;
    }

    // LISTAR COMUNAS
    public List<Comuna> listarComunas() {
        try {
            return comunaRepository.findAll();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al listar las comunas: " + e.getMessage(), e);
        }
    }

    // BUSCAR COMUNA POR ID
    public Comuna buscarPorId(Integer id) {
        try {
            return buscarComunaEntidad(id);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar la comuna: " + e.getMessage(), e);
        }
    }

    // GUARDAR COMUNA
    public Comuna guardarComuna(Comuna comuna) {
        try {
            comunaValidaciones.validarComuna(comuna);

            Region region = regionRepository.findById(
                    comuna.getRegion().getIdRegion()).orElseThrow(() ->
                    new RuntimeException("La region no existe"));

            comuna.setNombreComuna(comuna.getNombreComuna().trim());
            comuna.setRegion(region);

            return comunaRepository.save(comuna);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la comuna: " + e.getMessage(), e);
        }
    }

    // ACTUALIZAR COMUNA
    public Comuna actualizarComuna(Integer id, Comuna datos) {
        try {
            Comuna comuna = buscarComunaEntidad(id);

            comunaValidaciones.validarComuna(datos);

            Region region = regionRepository.findById(datos.getRegion().getIdRegion())
                    .orElseThrow(() ->new RuntimeException("La region no existe"));

            comuna.setNombreComuna(datos.getNombreComuna().trim());
            comuna.setRegion(region);

            return comunaRepository.save(comuna);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar la comuna: " + e.getMessage(), e);
        }
    }

    // ELIMINAR COMUNA
    public String eliminarComuna(Integer id) {
        try {
            Comuna comuna = buscarComunaEntidad(id);

            comunaRepository.delete(comuna);

            return "Comuna eliminada correctamente";

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar la comuna: " + e.getMessage(), e);
        }
    }

    // BUSCAR ENTIDAD POR ID
    private Comuna buscarComunaEntidad(Integer id) {
        return comunaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("No se encontro la comuna con ID: " + id));
    }
}
