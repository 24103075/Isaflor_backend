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
@Table (name = "region")
public class Region {

    @Id 
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id_region")
    private Integer idRegion;

    @NotBlank(message = "El nombre de la region es obligatorio")
    @Size(min = 2, max = 100,message = "El nombre de la region debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre_region", nullable = false, length = 100)
    private String nombreRegion;


    
}
