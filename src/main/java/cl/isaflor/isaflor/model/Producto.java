
package cl.isaflor.isaflor.model;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "producto")
public class Producto {

    // IDENTIFICADOR DEL PRODUCTO
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    // DATOS GENERALES
    @NotBlank(message = "El codigo del producto es obligatorio")
    @Size(min = 2, max = 20)
    @Column(name = "codigo", nullable = false, length = 20)
    private String codigoProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombreProducto;

    @NotBlank(message = "La descripcion del producto es obligatoria")
    @Size(min = 2, max = 255)
    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @NotNull(message = "El precio del producto es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser positivo")
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioProducto;

    // GESTION DE OFERTAS
    @Builder.Default
    @NotNull(message = "El estado de la oferta es obligatorio")
    @Column(name = "en_oferta", nullable = false)
    private Boolean enOferta = false;

    @DecimalMin(value = "0.01", message = "El precio de oferta debe ser positivo")
    @Digits(integer = 10, fraction = 2,
            message = "El precio de oferta tiene un formato invalido")
    @Column(name = "precio_oferta", precision = 12, scale = 2)
    private BigDecimal precioOferta;

    // INVENTARIO
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock", nullable = false)
    private Integer stockProducto;

    @NotNull(message = "El stock critico es obligatorio")
    @Min(value = 0, message = "El stock critico no puede ser negativo")
    @Column(name = "stock_critico", nullable = false)
    private Integer stockCritico;

    // PESOS DEL PRODUCTO (OPCIONALES)
    @DecimalMin(value = "0.01", message = "El peso debe ser positivo")
    @Column(name = "peso_normal", precision = 10, scale = 2)
    private BigDecimal pesoProducto;

    @DecimalMin(value = "0.01", message = "El peso volumetrico debe ser positivo")
    @Column(name = "peso_volumetrico", precision = 10, scale = 2)
    private BigDecimal pesoVolumetrico;

    // RELACION CON CATEGORIA
    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    // RELACION CON IMAGENES
    @OneToMany(mappedBy = "producto")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Imagen> imagenes;
}
