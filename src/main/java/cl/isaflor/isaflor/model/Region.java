package cl.isaflor.isaflor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "El nombre de la region es obligatorio")
    @Column(name = "nombre_region", nullable = false, length = 100)
    private String nombreRegion;


    
}
