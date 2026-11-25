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

@Entity 
@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
@Table (name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;
    
    @NotBlank(message="El nombre de la categoria es obligatoria")
    @Size(min=2,max=100)
    @Column(name = "nombre_categoria",length=100)
    private String nombreCategoria;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

}
