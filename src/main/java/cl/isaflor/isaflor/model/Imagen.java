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
@Table (name = "imagen")
public class Imagen {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Integer idImagen;

    @NotBlank(message = "El nombre de la imagen es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Size(max = 255)
    @Column(name = "url_imagen", nullable = false, length = 255)
    private String urlImagen;

    @Size(max = 255)
    @Column(length = 255)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
    
}
