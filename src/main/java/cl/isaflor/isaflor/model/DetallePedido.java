package cl.isaflor.isaflor.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data 
@AllArgsConstructor 
@Builder 
@NoArgsConstructor 
@Table (name = "detalle_pedido")
public class DetallePedido {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser como minimo 1")
    @Column(name = "cantidad",nullable = false)
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.01", message = "El subtotal debe ser mayor que cero")
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subTotal;

    @NotNull(message = "El pedido es obligatorio")
    @ManyToOne 
    @JoinColumn (name ="id_pedido", nullable=false)
    private Pedido pedido;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne 
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;


    
}
