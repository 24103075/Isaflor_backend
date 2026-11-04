package cl.isaflor.isaflor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Entity 
@AllArgsConstructor 
@NoArgsConstructor 
@Table (name = "comuna")
public class Comuna {

    @Id 
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idComuna;

    @Column (nullable = false, length = 50)
    private String nombreComuna;

    @ManyToOne// muchas comunas pueden pertenecer a una región
    @JoinColumn(name = "id_region", nullable = false)//crea la clave foránea id_region
    private Region region; 

    
    
    
}
