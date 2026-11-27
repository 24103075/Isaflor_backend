package cl.isaflor.isaflor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BlogRequestDTO {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 150)
    private String titulo;

    @NotBlank(message = "El contenido es obligatorio")
    private String contenido;
}
