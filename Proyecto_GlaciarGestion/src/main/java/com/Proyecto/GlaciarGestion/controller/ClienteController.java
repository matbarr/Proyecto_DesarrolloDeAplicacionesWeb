package com.Proyecto.GlaciarGestion.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Proyecto.GlaciarGestion.dto.AgregarCarritoRequest;
import com.Proyecto.GlaciarGestion.dto.CarritoResumenView;
import com.Proyecto.GlaciarGestion.dto.DireccionRequest;
import com.Proyecto.GlaciarGestion.model.DetallePedido;
import com.Proyecto.GlaciarGestion.model.Pedido;
import com.Proyecto.GlaciarGestion.model.Producto;
import com.Proyecto.GlaciarGestion.model.RolUsuario;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.service.BusinessException;
import com.Proyecto.GlaciarGestion.service.CarritoService;
import com.Proyecto.GlaciarGestion.service.DireccionService;
import com.Proyecto.GlaciarGestion.service.PedidoService;
import com.Proyecto.GlaciarGestion.service.ProductoService;
import com.Proyecto.GlaciarGestion.web.SessionService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final SessionService sessionService;
    private final ProductoService productoService;
    private final CarritoService carritoService;
    private final DireccionService direccionService;
    private final PedidoService pedidoService;

    public ClienteController(
        SessionService sessionService,
        ProductoService productoService,
        CarritoService carritoService,
        DireccionService direccionService,
        PedidoService pedidoService
    ) {
        this.sessionService = sessionService;
        this.productoService = productoService;
        this.carritoService = carritoService;
        this.direccionService = direccionService;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/productos")
    public String productos(HttpSession session, Model model) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("productos", productoService.listarActivos());
        if (!model.containsAttribute("agregarCarritoRequest")) {
            model.addAttribute("agregarCarritoRequest", new AgregarCarritoRequest());
        }
        return "cliente/productos";
    }

    @GetMapping("/productos/{productoId}")
    public String detalleProducto(HttpSession session, @PathVariable Long productoId, Model model) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            Producto producto = productoService.obtenerProductoActivo(productoId);
            model.addAttribute("usuario", usuario);
            model.addAttribute("producto", producto);
            return "cliente/detalle-producto";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("productos", productoService.listarActivos());
            model.addAttribute("agregarCarritoRequest", new AgregarCarritoRequest());
            return "cliente/productos";
        }
    }

    @PostMapping("/carrito/agregar")
    public String agregarCarrito(
        HttpSession session,
        @Valid @ModelAttribute("agregarCarritoRequest") AgregarCarritoRequest request,
        BindingResult bindingResult,
        Model model
    ) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("productos", productoService.listarActivos());
            return "cliente/productos";
        }

        try {
            carritoService.agregarProducto(usuario, request.getProductoId(), request.getCantidad());
            return "redirect:/cliente/carrito";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("productos", productoService.listarActivos());
            return "cliente/productos";
        }
    }

    @GetMapping("/carrito")
    public String carrito(HttpSession session, Model model) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        CarritoResumenView resumen = carritoService.obtenerResumen(usuario);
        model.addAttribute("usuario", usuario);
        model.addAttribute("resumen", resumen);
        model.addAttribute("direcciones", direccionService.listarPorUsuario(usuario));
        if (!model.containsAttribute("direccionRequest")) {
            model.addAttribute("direccionRequest", new DireccionRequest());
        }
        return "cliente/carrito";
    }
    @PostMapping("/carrito/actualizar")
public String actualizarCarrito(
    HttpSession session,
    @RequestParam Long detalleId,
    @RequestParam Integer cantidad,
    Model model
) {
    Usuario usuario = clienteAutenticado(session);
    if (usuario == null) {
        return "redirect:/login";
    }

    try {
        carritoService.actualizarCantidad(usuario, detalleId, cantidad);
        return "redirect:/cliente/carrito";
    } catch (BusinessException ex) {
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("usuario", usuario);
        model.addAttribute("resumen", carritoService.obtenerResumen(usuario));
        model.addAttribute("direcciones", direccionService.listarPorUsuario(usuario));
        model.addAttribute("direccionRequest", new DireccionRequest());
        return "cliente/carrito";
    }
}

@PostMapping("/carrito/eliminar")
public String eliminarDelCarrito(
    HttpSession session,
    @RequestParam Long detalleId,
    Model model
) {
    Usuario usuario = clienteAutenticado(session);
    if (usuario == null) {
        return "redirect:/login";
    }

    try {
        carritoService.eliminarProducto(usuario, detalleId);
        return "redirect:/cliente/carrito";
    } catch (BusinessException ex) {
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("usuario", usuario);
        model.addAttribute("resumen", carritoService.obtenerResumen(usuario));
        model.addAttribute("direcciones", direccionService.listarPorUsuario(usuario));
        model.addAttribute("direccionRequest", new DireccionRequest());
        return "cliente/carrito";
    }
}

    @PostMapping("/direcciones")
    public String registrarDireccion(
        HttpSession session,
        @Valid @ModelAttribute("direccionRequest") DireccionRequest direccionRequest,
        BindingResult bindingResult,
        Model model
    ) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("resumen", carritoService.obtenerResumen(usuario));
            model.addAttribute("direcciones", direccionService.listarPorUsuario(usuario));
            return "cliente/carrito";
        }

        direccionService.registrar(usuario, direccionRequest);
        return "redirect:/cliente/carrito";
    }

    @PostMapping("/pedidos/confirmar")
    public String confirmarPedido(HttpSession session, @RequestParam("direccionId") Long direccionId, Model model) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            pedidoService.confirmarPedido(usuario, direccionId);
            return "redirect:/cliente/pedidos";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("resumen", carritoService.obtenerResumen(usuario));
            model.addAttribute("direcciones", direccionService.listarPorUsuario(usuario));
            model.addAttribute("direccionRequest", new DireccionRequest());
            return "cliente/carrito";
        }
    }

    @GetMapping("/pedidos")
    public String pedidos(HttpSession session, Model model) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        List<Pedido> pedidos = pedidoService.listarPedidosCliente(usuario);
        model.addAttribute("pedidos", pedidos);
        return "cliente/pedidos";
    }

    @GetMapping("/pedidos/{pedidoId}/estado")
    public String estadoPedido(HttpSession session, @PathVariable Long pedidoId, Model model) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            Pedido pedido = pedidoService.obtenerPedidoCliente(pedidoId, usuario);
            model.addAttribute("pedido", pedido);
            return "cliente/estado-pedido";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("pedidos", pedidoService.listarPedidosCliente(usuario));
            return "cliente/pedidos";
        }
    }

    @GetMapping("/pedidos/{pedidoId}/detalle")
    public String detallePedido(HttpSession session, @PathVariable Long pedidoId, Model model) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            Pedido pedido = pedidoService.obtenerPedidoCliente(pedidoId, usuario);
            List<DetallePedido> detalles = pedidoService.obtenerDetallesPedidoCliente(pedidoId, usuario);
            model.addAttribute("pedido", pedido);
            model.addAttribute("detalles", detalles);
            return "cliente/detalle-pedido";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("pedidos", pedidoService.listarPedidosCliente(usuario));
            return "cliente/pedidos";
        }
    }

    private Usuario clienteAutenticado(HttpSession session) {
        if (!sessionService.tieneRol(session, RolUsuario.CLIENTE)) {
            return null;
        }
        return sessionService.obtenerUsuario(session);
    }
}
