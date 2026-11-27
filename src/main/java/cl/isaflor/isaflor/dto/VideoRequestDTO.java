package cl.isaflor.isaflor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VideoRequestDTO {

    @NotNull(message = "El blog es obligatorio")
    @Positive(message = "El ID del blog debe ser positivo")
    private Integer idBlog;

    @NotBlank(message = "El titulo del video es obligatorio")
    @Size(max = 150, message = "El titulo no puede superar los 150 caracteres")
    private String titulo;

    @NotBlank(message = "La URL del video es obligatoria")
    @Size(max = 255, message = "La URL no puede superar los 255 caracteres")
    private String urlVideo;

    @Size(max = 255, message = "La descripcion no puede superar los 255 caracteres")
    private String descripcion;
}
