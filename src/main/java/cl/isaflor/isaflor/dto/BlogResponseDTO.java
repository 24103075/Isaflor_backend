package cl.isaflor.isaflor.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BlogResponseDTO {

    private Integer idBlog;
    private String titulo;
    private String contenido;
    private LocalDateTime fechaPublicacion;
    private String estado;
}
