package cl.isaflor.isaflor.dto;

import lombok.Data;

@Data
public class ImagenBlogResponseDTO {

    private Integer idImagenBlog;
    private String nombre;
    private String urlImagen;
    private String descripcion;
    private Integer idBlog;
}
