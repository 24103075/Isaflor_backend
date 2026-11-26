package cl.isaflor.isaflor.service;

import org.springframework.stereotype.Service;

import cl.isaflor.isaflor.dto.ProductoDTO;
import cl.isaflor.isaflor.model.Producto;

@Service 
public class ProductoValidaciones {

    //Valida que los datos obligatorios de prodcuto no esten vacios
    public Boolean validarNullVacio(Producto producto){

        if (producto.getCodigoProducto() == null || producto.getCodigoProducto().trim().isEmpty()) {
            return false;
        }

        if (producto.getNombreProducto() == null || producto.getNombreProducto().trim().isEmpty()) {
            return false;
        }

        if (producto.getDescripcion() == null || producto.getDescripcion().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    public ProductoDTO convertirADTO (Producto producto){
        ProductoDTO productoDTO = new ProductoDTO();

        productoDTO.setIdProducto(producto.getIdProducto());
        productoDTO.setCodigoProducto(producto.getCodigoProducto());
        productoDTO.setNombreProducto(producto.getNombreProducto());
        productoDTO.setDescripcion(producto.getDescripcion());
        productoDTO.setPrecioProducto(producto.getPrecioProducto());
        productoDTO.setStockProducto(producto.getStockProducto());
        productoDTO.setStockCritico(producto.getStockCritico());
        productoDTO.setPesoProducto(producto.getPesoProducto());
        productoDTO.setPesoVolumetrico(producto.getPesoVolumetrico());

         if (producto.getCategoria() != null) {
            productoDTO.setIdCategoria(producto.getCategoria().getIdCategoria());

            productoDTO.setNombreCategoria(producto.getCategoria().getNombreCategoria());
        }
        return productoDTO;
    }
}
