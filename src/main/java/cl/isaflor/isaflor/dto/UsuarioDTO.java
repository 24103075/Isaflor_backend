package cl.isaflor.isaflor.dto;

import java.time.LocalDate;
import lombok.Data;

@Data 
public class UsuarioDTO {

    private Integer idUsuario;
    private String run;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correo;
    private LocalDate fechaNacimiento;
    private String tipoUsuario;   //Evaluar si se trabajara con admin,cliente y venderdor.
    private String direccion;
    private Integer idComuna;
}
