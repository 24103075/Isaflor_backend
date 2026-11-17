package cl.isaflor.isaflor.model;

@Entity
@builder
@allArgsConstructor
@NoArgsConstructor
@table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer idProducto;

    @NotBlank(message="El codigo del producto es obligatorio")
    @Size(min=2,max=50)
    @Column(nullable=false,length=60)
    private String codigoProducto;

    @NotBlank(message="El nombre del producto es obligatorio")
    @Size(min=2,max=50)
    @Column(nullable=false,length=60)
    private String nombreProducto;

    @NotBlank(message="La descripcion del producto es obligatoria")
    @Size(min=2,max=200)
    @Column(nullable=false,length=200)
    private String descripcion;

    @NotNull(message="El precio del producto es obligatorio")
    @DecimalMin(value="0.01", message="El precio del producto debe ser un valor positivo")
    @Column(nullable=false, precision=10, scale=2)
    private Double precioProducto;

    @NotNull(message="El stock del producto es obligatorio")
    @Min(value=0, message="El stock del producto no puede ser negativo")
    @Column(nullable=false)
    private Integer stockProducto;

    @NotNull(message="El stock critico del producto es obligatorio")
    @Min(value=0, message="El stock critico del producto no puede ser negativo")
    @Column(nullable=false)
    private Integer stockCritico;

    @NotNull(message="El peso del producto es obligatorio")
    @DecimalMin(value="0.01", message="El peso del producto debe ser un valor positivo")
    @Column(nullable=false, precision=10, scale=2)
    private Double pesoProducto;

    @NotNull(message="El peso volumetrico del producto es obligatorio")
    @DecimalMin(value="0.01", message="El peso volumetrico del producto debe ser un valor positivo")
    @Column(nullable=false, precision=10, scale=2)
    private Double pesoVolumetrico;

    @ManyToOne
    @JoinColumn(name="idCategoria", nullable=false)
    private Categoria categoria;


    
}
