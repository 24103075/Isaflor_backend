package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import cl.isaflor.isaflor.dto.RegionDTO;
import cl.isaflor.isaflor.model.Region;
import cl.isaflor.isaflor.repository.ComunaRepository;
import cl.isaflor.isaflor.repository.RegionRepository;

@Service
@Transactional
public class RegionService {

    private final RegionRepository regionRepository;
    private final ComunaRepository comunaRepository;
    private final RegionValidaciones regionValidaciones;

    public RegionService(
            RegionRepository regionRepository,
            ComunaRepository comunaRepository,
            RegionValidaciones regionValidaciones) {
        this.regionRepository = regionRepository;
        this.comunaRepository = comunaRepository;
        this.regionValidaciones = regionValidaciones;
    }

    @Transactional(readOnly = true)
    public List<RegionDTO> listarRegiones() {
        return regionRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegionDTO buscarPorId(Integer id) {
        return convertirADTO(buscarRegionEntidad(id));
    }

    public RegionDTO guardarRegion(Region datos) {
        regionValidaciones.validar(datos);

        String nombre = datos.getNombreRegion().trim();
        if (regionRepository.existsByNombreRegionIgnoreCase(nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una region con ese nombre");
        }

        Region nueva = new Region();
        nueva.setNombreRegion(nombre);
        return convertirADTO(regionRepository.save(nueva));
    }

    public RegionDTO actualizarRegion(Integer id, Region datos) {
        Region existente = buscarRegionEntidad(id);
        regionValidaciones.validar(datos);

        String nombre = datos.getNombreRegion().trim();
        if (regionRepository.existsByNombreRegionIgnoreCaseAndIdRegionNot(
                nombre, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otra region con ese nombre");
        }

        existente.setNombreRegion(nombre);
        return convertirADTO(regionRepository.save(existente));
    }

    public String eliminarRegion(Integer id) {
        Region region = buscarRegionEntidad(id);

        if (comunaRepository.existsByRegion_IdRegion(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar la region porque tiene comunas asociadas");
        }

        regionRepository.delete(region);
        return "Region eliminada correctamente";
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

    private RegionDTO convertirADTO(Region region) {
        RegionDTO dto = new RegionDTO();
        dto.setIdRegion(region.getIdRegion());
        dto.setNombreRegion(region.getNombreRegion());
        return dto;
    }
}
