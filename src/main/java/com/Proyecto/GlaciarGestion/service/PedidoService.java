package com.Proyecto.GlaciarGestion.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.GlaciarGestion.model.DetalleCarrito;
import com.Proyecto.GlaciarGestion.model.DetallePedido;
import com.Proyecto.GlaciarGestion.model.DireccionEntrega;
import com.Proyecto.GlaciarGestion.model.EstadoPedido;
import com.Proyecto.GlaciarGestion.model.Pedido;
import com.Proyecto.GlaciarGestion.model.Producto;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.repository.DetallePedidoRepository;
import com.Proyecto.GlaciarGestion.repository.PedidoRepository;
import com.Proyecto.GlaciarGestion.repository.ProductoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final CarritoService carritoService;
    private final DireccionService direccionService;

    public PedidoService(
        PedidoRepository pedidoRepository,
        DetallePedidoRepository detallePedidoRepository,
        ProductoRepository productoRepository,
        CarritoService carritoService,
        DireccionService direccionService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.productoRepository = productoRepository;
        this.carritoService = carritoService;
        this.direccionService = direccionService;
    }

    @Transactional
    public Pedido confirmarPedido(Usuario usuario, Long direccionId) {
        List<DetalleCarrito> detallesCarrito = carritoService.obtenerDetalles(usuario);
        if (detallesCarrito.isEmpty()) {
            throw new BusinessException("El carrito debe contener al menos un producto.");
        }

        DireccionEntrega direccion = direccionService.obtenerDireccionDelUsuario(direccionId, usuario);

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCarrito detalleCarrito : detallesCarrito) {
            Producto producto = detalleCarrito.getProducto();
            if (!producto.isActivo()) {
                throw new BusinessException("Un producto del carrito ya no esta disponible.");
            }
            if (detalleCarrito.getCantidad() > producto.getCantidad()) {
                throw new BusinessException("Stock insuficiente para " + producto.getNombre() + ".");
            }
            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(detalleCarrito.getCantidad())));
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEntrega(direccion);
        pedido.setFecha(LocalDateTime.now());
        pedido.setTotal(total);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        for (DetalleCarrito detalleCarrito : detallesCarrito) {
            Producto producto = detalleCarrito.getProducto();
            producto.setCantidad(producto.getCantidad() - detalleCarrito.getCantidad());
            productoRepository.save(producto);

            DetallePedido detallePedido = new DetallePedido();
            detallePedido.setPedido(pedidoGuardado);
            detallePedido.setProducto(producto);
            detallePedido.setCantidad(detalleCarrito.getCantidad());
            detallePedido.setPrecioUnitario(producto.getPrecio());
            detallePedidoRepository.save(detallePedido);
        }

        carritoService.vaciarCarrito(usuario);
        return pedidoGuardado;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosCliente(Usuario usuario) {
        return pedidoRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaDesc();
    }

    @Transactional
    public Pedido actualizarEstadoPedido(Long pedidoId, EstadoPedido estado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new BusinessException("Pedido no encontrado."));
        pedido.setEstado(estado);
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPedidoCliente(Long pedidoId, Usuario usuario) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new BusinessException("Pedido no encontrado."));

        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("El pedido no pertenece al cliente autenticado.");
        }

        return pedido;
    }

    @Transactional(readOnly = true)
    public List<DetallePedido> obtenerDetallesPedidoCliente(Long pedidoId, Usuario usuario) {
        Pedido pedido = obtenerPedidoCliente(pedidoId, usuario);
        return detallePedidoRepository.findByPedidoOrderByIdAsc(pedido);
    }
}
