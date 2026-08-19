package com.Proyecto.GlaciarGestion.controllers;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Proyecto.GlaciarGestion.dto.AgregarCarritoRequest;
import com.Proyecto.GlaciarGestion.dto.CarritoResumenView;
import com.Proyecto.GlaciarGestion.dto.ConsultaCrearRequest;
import com.Proyecto.GlaciarGestion.dto.ConsultaMensajeRequest;
import com.Proyecto.GlaciarGestion.dto.DireccionRequest;
import com.Proyecto.GlaciarGestion.domain.Consulta;
import com.Proyecto.GlaciarGestion.domain.DetallePedido;
import com.Proyecto.GlaciarGestion.domain.MensajeConsulta;
import com.Proyecto.GlaciarGestion.domain.Pedido;
import com.Proyecto.GlaciarGestion.domain.Producto;
import com.Proyecto.GlaciarGestion.domain.RolUsuario;
import com.Proyecto.GlaciarGestion.domain.Usuario;
import com.Proyecto.GlaciarGestion.service.BusinessException;
import com.Proyecto.GlaciarGestion.service.CarritoService;
import com.Proyecto.GlaciarGestion.service.ConsultaService;
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
    private final ConsultaService consultaService;

    public ClienteController(
        SessionService sessionService,
        ProductoService productoService,
        CarritoService carritoService,
        DireccionService direccionService,
        PedidoService pedidoService,
        ConsultaService consultaService
    ) {
        this.sessionService = sessionService;
        this.productoService = productoService;
        this.carritoService = carritoService;
        this.direccionService = direccionService;
        this.pedidoService = pedidoService;
        this.consultaService = consultaService;
    }

    @GetMapping("/inicio")
    public String inicio(HttpSession session, Model model) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        return "cliente/inicio";
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
            model.addAttribute("usuario", usuario);
            model.addAttribute("productos", productoService.listarActivos());
            return "cliente/productos";
        }

        try {
            carritoService.agregarProducto(usuario, request.getProductoId(), request.getCantidad());
            return "redirect:/cliente/carrito";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("usuario", usuario);
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
            model.addAttribute("usuario", usuario);
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
        model.addAttribute("usuario", usuario);
        model.addAttribute("pedidos", pedidos);
        return "cliente/pedidos";
    }

    @GetMapping("/consultas")
    public String consultas(
        HttpSession session,
        @RequestParam(value = "consultaId", required = false) Long consultaId,
        Model model
    ) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        List<Consulta> consultas = consultaService.listarConsultasCliente(usuario);
        Consulta consultaSeleccionada = null;

        if (consultaId != null) {
            try {
                consultaSeleccionada = consultaService.obtenerConsultaCliente(consultaId, usuario);
            } catch (BusinessException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        } else if (!consultas.isEmpty()) {
            consultaSeleccionada = consultas.get(0);
        }

        if (consultaSeleccionada != null) {
            consultaService.marcarLeidaPorCliente(consultaSeleccionada.getId(), usuario);
        }

        List<MensajeConsulta> mensajes = consultaSeleccionada == null
            ? List.of()
            : consultaService.listarMensajes(consultaSeleccionada);

        Set<Long> consultasNoLeidas = consultaService.obtenerIdsNoLeidosCliente(usuario);

        if (!model.containsAttribute("consultaCrearRequest")) {
            model.addAttribute("consultaCrearRequest", new ConsultaCrearRequest());
        }
        if (!model.containsAttribute("consultaMensajeRequest")) {
            model.addAttribute("consultaMensajeRequest", new ConsultaMensajeRequest());
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("consultas", consultas);
        model.addAttribute("consultaSeleccionada", consultaSeleccionada);
        model.addAttribute("mensajes", mensajes);
        model.addAttribute("consultasNoLeidas", consultasNoLeidas);
        return "cliente/consultas";
    }

    @PostMapping("/consultas")
    public String crearConsulta(
        HttpSession session,
        @Valid @ModelAttribute("consultaCrearRequest") ConsultaCrearRequest request,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("consultas", consultaService.listarConsultasCliente(usuario));
            model.addAttribute("consultaSeleccionada", null);
            model.addAttribute("mensajes", List.of());
            model.addAttribute("consultasNoLeidas", consultaService.obtenerIdsNoLeidosCliente(usuario));
            if (!model.containsAttribute("consultaMensajeRequest")) {
                model.addAttribute("consultaMensajeRequest", new ConsultaMensajeRequest());
            }
            return "cliente/consultas";
        }

        try {
            Consulta consulta = consultaService.crearConsulta(usuario, request.getAsunto(), request.getMensaje());
            redirectAttributes.addFlashAttribute("success", "Consulta enviada correctamente.");
            return "redirect:/cliente/consultas?consultaId=" + consulta.getId();
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/cliente/consultas";
        }
    }

    @PostMapping("/consultas/{consultaId}/mensajes")
    public String responderConsulta(
        HttpSession session,
        @PathVariable Long consultaId,
        @Valid @ModelAttribute("consultaMensajeRequest") ConsultaMensajeRequest request,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Debes escribir un mensaje para enviarlo.");
            return "redirect:/cliente/consultas?consultaId=" + consultaId;
        }

        try {
            consultaService.agregarMensajeCliente(consultaId, usuario, request.getMensaje());
            return "redirect:/cliente/consultas?consultaId=" + consultaId;
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/cliente/consultas?consultaId=" + consultaId;
        }
    }

    @PostMapping("/consultas/{consultaId}/cerrar")
    public String cerrarConsulta(
        HttpSession session,
        @PathVariable Long consultaId,
        RedirectAttributes redirectAttributes
    ) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            consultaService.cerrarConsultaCliente(consultaId, usuario);
            redirectAttributes.addFlashAttribute("success", "Consulta cerrada correctamente.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/cliente/consultas?consultaId=" + consultaId;
    }

    @PostMapping("/consultas/{consultaId}/reabrir")
    public String reabrirConsulta(
        HttpSession session,
        @PathVariable Long consultaId,
        RedirectAttributes redirectAttributes
    ) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            consultaService.reabrirConsultaCliente(consultaId, usuario);
            redirectAttributes.addFlashAttribute("success", "Consulta reabierta correctamente.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/cliente/consultas?consultaId=" + consultaId;
    }

    @GetMapping("/pedidos/{pedidoId}/estado")
    public String estadoPedido(HttpSession session, @PathVariable Long pedidoId, Model model) {
        Usuario usuario = clienteAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            Pedido pedido = pedidoService.obtenerPedidoCliente(pedidoId, usuario);
            model.addAttribute("usuario", usuario);
            model.addAttribute("pedido", pedido);
            return "cliente/estado-pedido";
        } catch (BusinessException ex) {
            model.addAttribute("usuario", usuario);
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
            model.addAttribute("usuario", usuario);
            model.addAttribute("pedido", pedido);
            model.addAttribute("detalles", detalles);
            return "cliente/detalle-pedido";
        } catch (BusinessException ex) {
            model.addAttribute("usuario", usuario);
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

