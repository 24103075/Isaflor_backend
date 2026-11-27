package cl.isaflor.isaflor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BlogEstadoRequestDTO {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(
            regexp = "(?i)^(BORRADOR|PUBLICADO)$",
            message = "El estado debe ser BORRADOR o PUBLICADO")
    private String estado;
}
