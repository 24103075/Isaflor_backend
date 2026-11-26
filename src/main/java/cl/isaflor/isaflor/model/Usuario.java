package cl.isaflor.isaflor.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;

@Data 
@Entity 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
@Table(name = "usuario")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @NotBlank(message = "El RUN es obligatorio")
    @Size(max = 12)
    @Column(name = "run", unique = true, nullable = false, length = 12)// se guarda sin puntos 12345678-9
    private String run;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 20)
    @Column(name = "apellido_paterno", nullable = false, length = 20)
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(max = 20)
    @Column(name = "apellido_materno", nullable = false, length = 20)
    private String apellidoMaterno;

    @NotBlank(message = "El correo es obligatorio")
    @Size(max = 100)
    @Column(name = "correo", unique = true, nullable = false, length = 100)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 100)
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    
    @NotBlank(message = "El tipo de usuario es obligatorio")
    @Size(max = 20)
    @Pattern( //permite solo los valores "cliente" o "administrador"
        regexp = "^(cliente|administrador)$",
        message = "El tipo de usuario debe ser cliente o administrador"
    )
    @Column(name = "tipo_usuario", nullable = false, length = 20)
    private String tipoUsuario;


    @Size(max = 300)
    @Column(name = "direccion", length = 300)
    private String direccion;

    @NotNull(message = "La comuna es obligatoria")
    @ManyToOne // Una Comuna pertenece a una Region.
    @JoinColumn(name = "id_comuna", nullable = false)
    private Comuna comuna; //idComuna es la llave foranea que hace referencia a la tabla comuna
    

}