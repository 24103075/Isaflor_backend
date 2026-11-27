package cl.isaflor.isaflor.dto;

import lombok.Data;

@Data
public class VideoResponseDTO {

    private Integer idVideo;
    private Integer idBlog;
    private String titulo;
    private String urlVideo;
    private String descripcion;
}
