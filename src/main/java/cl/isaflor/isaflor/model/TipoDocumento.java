package cl.isaflor.isaflor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Entity 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
@Table (name = "tipoDocumento")
public class TipoDocumento {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_documento")
    private Integer idTipoDocumento;

    @NotBlank(message = "El nombre del documento es obligatorio")
    @Size(max = 50)
    @Column(name = "nombre_documento", nullable = false, length = 50)
    private String nombreDocumento;

    @Size(max = 150)
    @Column(length = 150)
    private String descripcion;
    
}
