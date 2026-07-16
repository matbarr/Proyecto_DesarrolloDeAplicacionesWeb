package com.Proyecto.GlaciarGestion.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.GlaciarGestion.dto.CarritoItemView;
import com.Proyecto.GlaciarGestion.dto.CarritoResumenView;
import com.Proyecto.GlaciarGestion.model.Carrito;
import com.Proyecto.GlaciarGestion.model.DetalleCarrito;
import com.Proyecto.GlaciarGestion.model.Producto;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.repository.CarritoRepository;
import com.Proyecto.GlaciarGestion.repository.DetalleCarritoRepository;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final DetalleCarritoRepository detalleCarritoRepository;
    private final ProductoService productoService;

    public CarritoService(
        CarritoRepository carritoRepository,
        DetalleCarritoRepository detalleCarritoRepository,
        ProductoService productoService
    ) {
        this.carritoRepository = carritoRepository;
        this.detalleCarritoRepository = detalleCarritoRepository;
        this.productoService = productoService;
    }

    @Transactional
    public void agregarProducto(Usuario usuario, Long productoId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new BusinessException("La cantidad debe ser mayor que cero.");
        }

        Producto producto = productoService.obtenerProductoActivo(productoId);
        Carrito carrito = obtenerOCrearCarrito(usuario);

        DetalleCarrito detalle = detalleCarritoRepository.findByCarritoAndProducto(carrito, producto)
            .orElseGet(() -> {
                DetalleCarrito nuevo = new DetalleCarrito();
                nuevo.setCarrito(carrito);
                nuevo.setProducto(producto);
                nuevo.setCantidad(0);
                return nuevo;
            });

        int nuevaCantidad = detalle.getCantidad() + cantidad;
        if (nuevaCantidad > producto.getCantidad()) {
            throw new BusinessException("No se puede agregar una cantidad superior a la disponible.");
        }

        detalle.setCantidad(nuevaCantidad);
        detalleCarritoRepository.save(detalle);
    }
    @Transactional
public void actualizarCantidad(Usuario usuario, Long detalleId, Integer cantidad) {
    if (cantidad == null || cantidad <= 0) {
        throw new BusinessException("La cantidad debe ser mayor que cero.");
    }

    DetalleCarrito detalle = detalleCarritoRepository.findById(detalleId)
        .orElseThrow(() -> new BusinessException("El producto no existe en el carrito."));

    if (!detalle.getCarrito().getUsuario().getId().equals(usuario.getId())) {
        throw new BusinessException("No tiene permiso para modificar este carrito.");
    }

    Producto producto = detalle.getProducto();

    if (cantidad > producto.getCantidad()) {
        throw new BusinessException("No hay suficiente inventario disponible.");
    }

    detalle.setCantidad(cantidad);
    detalleCarritoRepository.save(detalle);
}

@Transactional
public void eliminarProducto(Usuario usuario, Long detalleId) {
    DetalleCarrito detalle = detalleCarritoRepository.findById(detalleId)
        .orElseThrow(() -> new BusinessException("El producto no existe en el carrito."));

    if (!detalle.getCarrito().getUsuario().getId().equals(usuario.getId())) {
        throw new BusinessException("No tiene permiso para eliminar este producto.");
    }

    detalleCarritoRepository.delete(detalle);
}

    @Transactional(readOnly = true)
    public CarritoResumenView obtenerResumen(Usuario usuario) {
        Carrito carrito = carritoRepository.findByUsuario(usuario).orElse(null);
        CarritoResumenView resumen = new CarritoResumenView();

        if (carrito == null) {
            return resumen;
        }

        List<DetalleCarrito> detalles = detalleCarritoRepository.findByCarritoOrderByIdAsc(carrito);
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleCarrito detalle : detalles) {
            CarritoItemView item = new CarritoItemView();
            item.setDetalleId(detalle.getId());
            item.setProductoId(detalle.getProducto().getId());
            item.setProductoNombre(detalle.getProducto().getNombre());
            item.setCantidad(detalle.getCantidad());
            item.setPrecioUnitario(detalle.getProducto().getPrecio());
            BigDecimal subtotal = detalle.getProducto().getPrecio().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            item.setSubtotal(subtotal);
            total = total.add(subtotal);
            resumen.getItems().add(item);
        }

        resumen.setTotal(total);
        return resumen;
    }

    @Transactional
    public void vaciarCarrito(Usuario usuario) {
        Carrito carrito = carritoRepository.findByUsuario(usuario).orElse(null);
        if (carrito != null) {
            detalleCarritoRepository.deleteByCarrito(carrito);
        }
    }

    @Transactional(readOnly = true)
    public List<DetalleCarrito> obtenerDetalles(Usuario usuario) {
        Carrito carrito = carritoRepository.findByUsuario(usuario).orElse(null);
        if (carrito == null) {
            return List.of();
        }
        return detalleCarritoRepository.findByCarritoOrderByIdAsc(carrito);
    }

    @Transactional
    public Carrito obtenerOCrearCarrito(Usuario usuario) {
        return carritoRepository.findByUsuario(usuario)
            .orElseGet(() -> {
                Carrito carrito = new Carrito();
                carrito.setUsuario(usuario);
                carrito.setCreadoEn(LocalDateTime.now());
                return carritoRepository.save(carrito);
            });
    }
}
