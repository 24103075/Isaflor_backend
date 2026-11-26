
package cl.isaflor.isaflor.dto;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroUsuarioDTO {

    @NotBlank(message = "El RUN es obligatorio")
    @Size(max = 12, message = "El RUN no puede superar los 12 caracteres")
    private String run;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 20, message = "El apellido paterno no puede superar los 20 caracteres")
    private String apellidoPaterno;

    @NotBlank
    @Column(name = "apellido_materno", nullable = false, length = 20)
    private String apellidoMaterno;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Size(max = 100)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotNull(message = "La comuna es obligatoria")
    @Positive(message = "La comuna debe tener un ID válido")
    private Integer idComuna;
}
