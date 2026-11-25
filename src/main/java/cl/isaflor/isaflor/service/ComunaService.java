package cl.isaflor.isaflor.service;

import java.util.List;

import cl.isaflor.isaflor.model.Comuna;
import cl.isaflor.isaflor.repository.ComunaRepository;

public class ComunaService {
    
    private final ComunaRepository comunaRepository;

    public ComunaService(ComunaRepository comunaRepository) {
        this.comunaRepository = comunaRepository;
    }

    // Listar todas las comunas
    public List<Comuna> listarComunas() {
        return comunaRepository.findAll();
    }

    // Buscar comuna por ID
    public Comuna buscarPorId(Integer id) {
        return comunaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró la comuna con ID: " + id));
    }

    // Registrar comuna
    public Comuna guardarComuna(Comuna comuna) {
        return comunaRepository.save(comuna);
    }

    // Actualizar comuna
    public Comuna actualizarComuna(Integer id, Comuna datos) {

        Comuna comuna = buscarPorId(id);

        comuna.setNombreComuna(datos.getNombreComuna());
        comuna.setRegion(datos.getRegion());

        return comunaRepository.save(comuna);
    }

    // Eliminar comuna
    public void eliminarComuna(Integer id) {
        Comuna comuna = buscarPorId(id);
        comunaRepository.delete(comuna);
    }
}
