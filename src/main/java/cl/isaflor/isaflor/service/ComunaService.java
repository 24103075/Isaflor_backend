
package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Transactional(readOnly = true)
    public List<Comuna> listarComunas() {
        return comunaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Comuna buscarPorId(Integer id) {
        return buscarComunaEntidad(id);
    }

    public Comuna guardarComuna(Comuna comuna) {
        comunaValidaciones.validarComuna(comuna);

        Region region = buscarRegionEntidad(
                comuna.getRegion().getIdRegion());

        comuna.setNombreComuna(comuna.getNombreComuna().trim());
        comuna.setRegion(region);

        return comunaRepository.save(comuna);
    }

    public Comuna actualizarComuna(Integer id, Comuna datos) {
        Comuna existente = buscarComunaEntidad(id);

        comunaValidaciones.validarComuna(datos);

        Region region = buscarRegionEntidad(
                datos.getRegion().getIdRegion());

        existente.setNombreComuna(datos.getNombreComuna().trim());
        existente.setRegion(region);

        return comunaRepository.save(existente);
    }

    public String eliminarComuna(Integer id) {
        Comuna comuna = buscarComunaEntidad(id);

        comunaRepository.delete(comuna);

        return "Comuna eliminada correctamente";
    }

    private Comuna buscarComunaEntidad(Integer id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID de la comuna debe ser valido");
        }

        return comunaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro la comuna con ID: " + id));
    }

    private Region buscarRegionEntidad(Integer id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID de la region debe ser valido");
        }

        return regionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontro la region con ID: " + id));
    }
}
