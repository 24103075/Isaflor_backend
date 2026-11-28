
package cl.isaflor.isaflor.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import cl.isaflor.isaflor.dto.ProductoDTO;
import cl.isaflor.isaflor.model.Producto;

@Service
public class ProductoValidaciones {

    public Boolean validarNullVacio(Producto producto) {

        if (producto.getCodigoProducto() == null ||
            producto.getCodigoProducto().trim().isEmpty()) {
            return false;
        }

        if (producto.getNombreProducto() == null ||
            producto.getNombreProducto().trim().isEmpty()) {
            return false;
        }

        if (producto.getDescripcion() == null ||
            producto.getDescripcion().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    public void validarOferta(Producto producto) {

        Boolean enOferta = producto.getEnOferta();
        BigDecimal precioOriginal = producto.getPrecioProducto();
        BigDecimal precioOferta = producto.getPrecioOferta();

        if (enOferta == null) {
            throw new IllegalArgumentException(
                "El estado de la oferta es obligatorio"
            );
        }

        if (!enOferta) {
            if (precioOferta != null) {
                throw new IllegalArgumentException(
                    "Un producto sin oferta no debe tener precio de oferta"
                );
            }
            return;
        }

        if (precioOriginal == null ||
            precioOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "El precio original debe ser mayor a cero"
            );
        }

        if (precioOferta == null) {
            throw new IllegalArgumentException(
                "Debe ingresar un precio para activar la oferta"
            );
        }

        if (precioOferta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "El precio de oferta debe ser mayor a cero"
            );
        }

        if (precioOferta.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                "El precio de oferta no puede tener mas de dos decimales"
            );
        }

        if (precioOferta.compareTo(precioOriginal) >= 0) {
            throw new IllegalArgumentException(
                "El precio de oferta debe ser menor al precio original"
            );
        }
    }

    public ProductoDTO convertirADTO(Producto producto) {

        ProductoDTO productoDTO = new ProductoDTO();

        productoDTO.setIdProducto(producto.getIdProducto());
        productoDTO.setCodigoProducto(producto.getCodigoProducto());
        productoDTO.setNombreProducto(producto.getNombreProducto());
        productoDTO.setDescripcion(producto.getDescripcion());
        productoDTO.setPrecioProducto(producto.getPrecioProducto());

        // Datos de la oferta

        productoDTO.setEnOferta(producto.getEnOferta());
        productoDTO.setPrecioOferta(producto.getPrecioOferta());

        // Datos de inventario

        productoDTO.setStockProducto(producto.getStockProducto());
        productoDTO.setStockCritico(producto.getStockCritico());
        productoDTO.setPesoProducto(producto.getPesoProducto());
        productoDTO.setPesoVolumetrico(producto.getPesoVolumetrico());

        // Datos de categoria

        if (producto.getCategoria() != null) {

            productoDTO.setIdCategoria(
                producto.getCategoria().getIdCategoria()
            );

            productoDTO.setNombreCategoria(
                producto.getCategoria().getNombreCategoria()
            );
        }

        if (producto.getImagenes() != null) {

            List<String> imagenes = producto.getImagenes()
                .stream()
                .map(imagen -> imagen.getUrlImagen())
                .toList();

            productoDTO.setImagenes(imagenes);

        } else {

            productoDTO.setImagenes(List.of());

        }

        return productoDTO;
    }
}
