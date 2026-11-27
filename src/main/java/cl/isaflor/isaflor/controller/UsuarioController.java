
package cl.isaflor.isaflor.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.isaflor.isaflor.dto.UsuarioDTO;
import cl.isaflor.isaflor.model.Usuario;
import cl.isaflor.isaflor.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Operaciones para la gestion de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // LISTAR USUARIOS
    @GetMapping
    @Operation(summary = "Listar usuarios",description = "Obtiene todos los usuarios registrados")
    @ApiResponse(responseCode = "200", description = "Usuarios obtenidos correctamente")
    @ApiResponse(responseCode = "204", description = "No existen usuarios registrados")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {

        List<UsuarioDTO> usuarios = usuarioService.listarUsuarios();

        if (usuarios.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    // BUSCAR USUARIO POR ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID",description = "Obtiene un usuario segun su identificador")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {

        try {
            UsuarioDTO usuario = usuarioService.buscarPorId(id);
            return new ResponseEntity<>(usuario, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // GUARDAR USUARIO
    @PostMapping
    @Operation(summary = "Crear usuario",description = "Registra un nuevo usuario")
    @ApiResponse(responseCode = "201", description = "Usuario creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    public ResponseEntity<?> guardarUsuario(@Valid @RequestBody Usuario usuario) {

        try {
            UsuarioDTO nuevoUsuario = usuarioService.guardarUsuario(usuario);
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error al guardar: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ACTUALIZAR USUARIO
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario",description = "Actualiza los datos de un usuario existente")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody Usuario usuario) {

        try {
            UsuarioDTO actualizado = usuarioService.actualizarUsuario(id, usuario);
            return new ResponseEntity<>(actualizado, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error al actualizar: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // ELIMINAR USUARIO
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario",description = "Elimina un usuario segun su identificador")
    @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id) {

        try {
            usuarioService.eliminarUsuario(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error al eliminar: " + e.getMessage(),
                    HttpStatus.NOT_FOUND);
        }
    }
}
