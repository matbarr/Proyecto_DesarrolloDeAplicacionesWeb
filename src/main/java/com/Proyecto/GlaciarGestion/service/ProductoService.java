package com.Proyecto.GlaciarGestion.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long productoId, Producto productoDatos) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new BusinessException("Producto no encontrado."));

        producto.setNombre(productoDatos.getNombre());
        producto.setPresentacion(productoDatos.getPresentacion());
        producto.setPrecio(productoDatos.getPrecio());
        producto.setCantidad(productoDatos.getCantidad());
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto desactivar(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new BusinessException("Producto no encontrado."));
        producto.setActivo(false);
        return productoRepository.save(producto);
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
}
