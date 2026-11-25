package cl.isaflor.isaflor.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
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
@Table (name = "metodoEnvio")
public class MetodoEnvio {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_envio")
    private Integer idMetodoEnvio;

    @NotBlank(message = "El nombre del método de envío es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 255)
    @Column(length = 255)
    private String descripcion;

    @NotNull(message = "El costo del envío es obligatorio")
    @DecimalMin(value = "0.00", message = "El costo no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @NotBlank(message = "El tiempo de entrega es obligatorio")
    @Size(max = 50)
    @Column(name = "tiempo_entrega", nullable = false, length = 50)
    private String tiempoEntrega;
    
}
