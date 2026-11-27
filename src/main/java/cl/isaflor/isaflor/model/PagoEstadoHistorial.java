package cl.isaflor.isaflor.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "pago_estado_historial")
public class PagoEstadoHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial_pago")
    private Integer idHistorialPago;

    @ManyToOne
    @JoinColumn(name = "id_pago", nullable = false)
    private Pago pago;

    @Size(max = 30)
    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @NotNull
    @Size(max = 30)
    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private String estadoNuevo;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @NotNull
    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Size(max = 255)
    @Column(name = "motivo", length = 255)
    private String motivo;
}
