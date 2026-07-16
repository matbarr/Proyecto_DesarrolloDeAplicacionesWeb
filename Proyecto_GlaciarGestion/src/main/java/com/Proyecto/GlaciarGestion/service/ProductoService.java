package com.Proyecto.GlaciarGestion.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Proyecto.GlaciarGestion.dto.ProductoRequest;
import com.Proyecto.GlaciarGestion.model.Producto;
import com.Proyecto.GlaciarGestion.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Producto obtenerProductoActivo(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new BusinessException("Producto no encontrado."));

        if (!producto.isActivo()) {
            throw new BusinessException("Producto no disponible.");
        }

        return producto;
    }
    @Transactional(readOnly = true)
public Producto obtenerPorId(Long productoId) {
    return productoRepository.findById(productoId)
        .orElseThrow(() -> new BusinessException("Producto no encontrado."));
}

@Transactional
public Producto registrarProducto(ProductoRequest request) {
    Producto producto = new Producto();

    producto.setNombre(request.getNombre());
    producto.setPresentacion(request.getPresentacion());
    producto.setPrecio(request.getPrecio());
    producto.setCantidad(request.getCantidad());
    producto.setActivo(request.isActivo());

    return productoRepository.save(producto);
}

@Transactional
public Producto actualizarProducto(Long productoId, ProductoRequest request) {
    Producto producto = obtenerPorId(productoId);

    producto.setNombre(request.getNombre());
    producto.setPresentacion(request.getPresentacion());
    producto.setPrecio(request.getPrecio());
    producto.setCantidad(request.getCantidad());
    producto.setActivo(request.isActivo());

    return productoRepository.save(producto);
}

@Transactional
public void desactivarProducto(Long productoId) {
    Producto producto = obtenerPorId(productoId);
    producto.setActivo(false);
    productoRepository.save(producto);
}

public ProductoRequest crearRequestDesdeProducto(Producto producto) {
    ProductoRequest request = new ProductoRequest();

    request.setNombre(producto.getNombre());
    request.setPresentacion(producto.getPresentacion());
    request.setPrecio(producto.getPrecio());
    request.setCantidad(producto.getCantidad());
    request.setActivo(producto.isActivo());

    return request;
}
}
