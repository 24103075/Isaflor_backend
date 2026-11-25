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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Entity 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
@Table(name = "usuario")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(unique = true, nullable = false, length = 10)// se guarda sin puntos 12345678-9
    private String run;

    @Column(nullable = false, length = 20)
    private String nombre;

    @Column(name = "apellido_paterno", nullable = false, length = 20)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 20)
    private String apellidoMaterno;

    @Column(unique = true, nullable = false, length = 50)
    private String correo;

   @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "tipo_usuario", nullable = false, length = 20)
    private String tipoUsuario;   //Evaluar si se trabajara con admin,cliente y venderdor.

    @Column(length = 100)
    private String direccion;

    @ManyToOne // Una Comuna pertenece a una Region.
    @JoinColumn(name = "id_comuna", nullable = false)
    private Comuna comuna; //idComuna es la llave foranea que hace referencia a la tabla comuna
    

}