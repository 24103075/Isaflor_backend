
package cl.isaflor.isaflor.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cl.isaflor.isaflor.model.Imagen;
import cl.isaflor.isaflor.model.Producto;
import cl.isaflor.isaflor.repository.ImagenRepository;
import cl.isaflor.isaflor.repository.ProductoRepository;

@Service
public class ImagenService {

    private final ImagenRepository imagenRepository;
    private final ProductoRepository productoRepository;
    private final ImgBBService imgBBService;

    public ImagenService(
            ImagenRepository imagenRepository,
            ProductoRepository productoRepository,
            ImgBBService imgBBService) {

        this.imagenRepository = imagenRepository;
        this.productoRepository = productoRepository;
        this.imgBBService = imgBBService;
    }

    // LISTAR IMAGENES
    public List<Imagen> listarImagenes() {
        try {
            return imagenRepository.findAll();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al listar las imagenes: " + e.getMessage(), e);
        }
    }

    // BUSCAR IMAGEN POR ID
    public Imagen buscarPorId(Integer id) {
        try {
            return buscarImagenEntidad(id);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar la imagen: " + e.getMessage(), e);
        }
    }

    // SUBIR IMAGEN Y ASOCIARLA A UN PRODUCTO
    public Imagen guardarImagen(
            Integer idProducto,
            MultipartFile archivo,
            String descripcion) {

        try {
            if (idProducto == null) {
                throw new IllegalArgumentException("Debe indicar el producto");
            }

            if (archivo == null || archivo.isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar una imagen");
            }

            if (archivo.getOriginalFilename() == null ||
                    archivo.getOriginalFilename().isBlank()) {
                throw new IllegalArgumentException("El nombre del archivo es obligatorio");
            }

            if (descripcion != null && descripcion.length() > 255) {
                throw new IllegalArgumentException("La descripcion no puede superar los 255 caracteres");
            }

            Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("El producto no existe"));

            String url = imgBBService.subirImagen(archivo);

            Imagen imagen = new Imagen();
            imagen.setProducto(producto);
            imagen.setNombre(archivo.getOriginalFilename());
            imagen.setUrlImagen(url);
            imagen.setDescripcion(descripcion);

            return imagenRepository.save(imagen);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage(), e);
        }
    }

    // ELIMINAR REGISTRO DE IMAGEN
    public String eliminarImagen(Integer id) {
        try {
            Imagen imagen = buscarImagenEntidad(id);
            imagenRepository.delete(imagen);
            return "Imagen eliminada correctamente";
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar la imagen: " + e.getMessage(), e);
        }
    }

    // BUSCAR IMAGEN POR ID
    private Imagen buscarImagenEntidad(Integer id) {
        return imagenRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("No se encontro la imagen con ID: " + id));
    }
}
