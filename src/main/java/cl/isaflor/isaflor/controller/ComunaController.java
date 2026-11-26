
package cl.isaflor.isaflor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.isaflor.isaflor.model.Comuna;
import cl.isaflor.isaflor.service.ComunaService;

@RestController
@RequestMapping("/api/v1/comunas")
public class ComunaController {

    private final ComunaService comunaService;

    public ComunaController(ComunaService comunaService) {
        this.comunaService = comunaService;
    }

    // Listar todas las comunas
    @GetMapping
    public ResponseEntity<List<Comuna>> listarComunas() {
        return ResponseEntity.ok(comunaService.listarComunas());
    }

    // Buscar comuna por ID
    @GetMapping("/{id}")
    public ResponseEntity<Comuna> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(comunaService.buscarPorId(id));
    }

    // Registrar comuna
    @PostMapping
    public ResponseEntity<Comuna> guardarComuna(@RequestBody Comuna comuna) {
        Comuna nuevaComuna = comunaService.guardarComuna(comuna);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaComuna);
    }

    // Actualizar comuna
    @PutMapping("/{id}")
    public ResponseEntity<Comuna> actualizarComuna(
            @PathVariable Integer id,
            @RequestBody Comuna comuna) {

        return ResponseEntity.ok(comunaService.actualizarComuna(id, comuna));
    }

    // Eliminar comuna
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarComuna(@PathVariable Integer id) {
        comunaService.eliminarComuna(id);
        return ResponseEntity.noContent().build();
    }
}
