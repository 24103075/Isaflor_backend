package cl.isaflor.isaflor.model;

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

@Data 
@Entity 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
@Table (name = "comuna")
public class Comuna {

    @Id 
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id_comuna")
    private Integer idComuna;

    @NotBlank(message = "El nombre de la comuna es obligatorio")
    @Size(max = 100)
    @Column(name = "nombre_comuna", nullable = false, length = 100)
    private String nombreComuna;

    @NotNull(message = "La region es obligatoria")
    @ManyToOne// muchas comunas pueden pertenecer a una región
    @JoinColumn(name = "id_region", nullable = false)//crea la clave foránea id_region
    private Region region; 

    
    
    
}
