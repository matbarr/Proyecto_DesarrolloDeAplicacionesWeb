package com.Proyecto.GlaciarGestion.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Proyecto.GlaciarGestion.dto.ConsultaMensajeRequest;
import com.Proyecto.GlaciarGestion.dto.PedidoManualDesdeConsultaRequest;
import com.Proyecto.GlaciarGestion.dto.ProductoRequest;
import com.Proyecto.GlaciarGestion.domain.Consulta;
import com.Proyecto.GlaciarGestion.domain.DireccionEntrega;
import com.Proyecto.GlaciarGestion.domain.Producto;
import com.Proyecto.GlaciarGestion.domain.RolUsuario;
import com.Proyecto.GlaciarGestion.domain.Usuario;
import com.Proyecto.GlaciarGestion.service.BusinessException;
import com.Proyecto.GlaciarGestion.service.ConsultaService;
import com.Proyecto.GlaciarGestion.service.DireccionService;
import com.Proyecto.GlaciarGestion.service.ProductoService;
import com.Proyecto.GlaciarGestion.web.SessionService;
import com.Proyecto.GlaciarGestion.domain.DetallePedido;
import com.Proyecto.GlaciarGestion.domain.EstadoPedido;
import com.Proyecto.GlaciarGestion.domain.MensajeConsulta;
import com.Proyecto.GlaciarGestion.domain.Pedido;
import com.Proyecto.GlaciarGestion.service.PedidoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final SessionService sessionService;
    private final ProductoService productoService;
    private final PedidoService pedidoService;
    private final ConsultaService consultaService;
    private final DireccionService direccionService;

    public AdminController(
    SessionService sessionService,
    ProductoService productoService,
    PedidoService pedidoService,
    ConsultaService consultaService,
    DireccionService direccionService
) {
    this.sessionService = sessionService;
    this.productoService = productoService;
    this.pedidoService = pedidoService;
    this.consultaService = consultaService;
    this.direccionService = direccionService;
}

    @GetMapping("/productos")
    public String listarProductos(HttpSession session, Model model) {
        Usuario usuario = adminAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("productos", productoService.listarTodos());
        return "admin/productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevoProducto(HttpSession session, Model model) {
        Usuario usuario = adminAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("productoRequest", new ProductoRequest());
        model.addAttribute("modo", "nuevo");
        return "admin/producto-form";
    }

    @PostMapping("/productos")
    public String registrarProducto(
        HttpSession session,
        @Valid @ModelAttribute("productoRequest") ProductoRequest productoRequest,
        BindingResult bindingResult,
        Model model
    ) {
        Usuario usuario = adminAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("modo", "nuevo");
            return "admin/producto-form";
        }

        try {
            productoService.registrarProducto(productoRequest);
            return "redirect:/admin/productos";
        } catch (BusinessException ex) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("modo", "nuevo");
            return "admin/producto-form";
        }
    }

    @GetMapping("/productos/{productoId}/editar")
    public String editarProducto(
        HttpSession session,
        @PathVariable Long productoId,
        Model model
    ) {
        Usuario usuario = adminAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            Producto producto = productoService.obtenerPorId(productoId);
            ProductoRequest request = productoService.crearRequestDesdeProducto(producto);

            model.addAttribute("usuario", usuario);
            model.addAttribute("producto", producto);
            model.addAttribute("productoRequest", request);
            model.addAttribute("modo", "editar");
            return "admin/producto-form";
        } catch (BusinessException ex) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("productos", productoService.listarTodos());
            return "admin/productos";
        }
    }

    @PostMapping("/productos/{productoId}")
    public String actualizarProducto(
        HttpSession session,
        @PathVariable Long productoId,
        @Valid @ModelAttribute("productoRequest") ProductoRequest productoRequest,
        BindingResult bindingResult,
        Model model
    ) {
        Usuario usuario = adminAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            Producto producto = productoService.obtenerPorId(productoId);

            if (bindingResult.hasErrors()) {
                model.addAttribute("usuario", usuario);
                model.addAttribute("producto", producto);
                model.addAttribute("modo", "editar");
                return "admin/producto-form";
            }

            productoService.actualizarProducto(productoId, productoRequest);
            return "redirect:/admin/productos";
        } catch (BusinessException ex) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("productos", productoService.listarTodos());
            return "admin/productos";
        }
    }

    @PostMapping("/productos/{productoId}/desactivar")
    public String desactivarProducto(
        HttpSession session,
        @PathVariable Long productoId,
        Model model
    ) {
        Usuario usuario = adminAutenticado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            productoService.desactivarProducto(productoId);
        } catch (BusinessException ex) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("productos", productoService.listarTodos());
            return "admin/productos";
        }

        return "redirect:/admin/productos";
    }
    @GetMapping("/pedidos")
public String listarPedidos(HttpSession session, Model model) {
    Usuario usuario = adminAutenticado(session);
    if (usuario == null) {
        return "redirect:/login";
    }

    model.addAttribute("usuario", usuario);
    model.addAttribute("pedidos", pedidoService.listarTodosPedidos());
    model.addAttribute("estados", EstadoPedido.values());

    return "admin/pedidos";
}

@GetMapping("/pedidos/{pedidoId}")
public String detallePedidoAdmin(
    HttpSession session,
    @PathVariable Long pedidoId,
    Model model
) {
    Usuario usuario = adminAutenticado(session);
    if (usuario == null) {
        return "redirect:/login";
    }

    try {
        Pedido pedido = pedidoService.obtenerPedido(pedidoId);
        List<DetallePedido> detalles = pedidoService.obtenerDetallesPedido(pedidoId);

        model.addAttribute("usuario", usuario);
        model.addAttribute("pedido", pedido);
        model.addAttribute("detalles", detalles);
        model.addAttribute("estados", EstadoPedido.values());

        return "admin/pedido-detalle";
    } catch (BusinessException ex) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("pedidos", pedidoService.listarTodosPedidos());
        model.addAttribute("estados", EstadoPedido.values());

        return "admin/pedidos";
    }
}

@PostMapping("/pedidos/{pedidoId}/estado")
public String actualizarEstadoPedido(
    HttpSession session,
    @PathVariable Long pedidoId,
    @RequestParam EstadoPedido estado,
    Model model
) {
    Usuario usuario = adminAutenticado(session);
    if (usuario == null) {
        return "redirect:/login";
    }

    try {
        pedidoService.actualizarEstado(pedidoId, estado);
    } catch (BusinessException ex) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("pedidos", pedidoService.listarTodosPedidos());
        model.addAttribute("estados", EstadoPedido.values());
        return "admin/pedidos";
    }

    return "redirect:/admin/pedidos/" + pedidoId;
}

@GetMapping("/consultas")
public String consultas(
    HttpSession session,
    @RequestParam(value = "consultaId", required = false) Long consultaId,
    @RequestParam(value = "cliente", required = false) String cliente,
    @RequestParam(value = "estado", defaultValue = "todas") String estado,
    @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
    @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
    Model model
) {
    Usuario usuario = adminAutenticado(session);
    if (usuario == null) {
        return "redirect:/login";
    }

    Boolean soloAbiertas = null;
    if ("abiertas".equalsIgnoreCase(estado)) {
        soloAbiertas = true;
    } else if ("cerradas".equalsIgnoreCase(estado)) {
        soloAbiertas = false;
    }

    List<Consulta> consultas = consultaService.listarConsultasAdminFiltradas(cliente, soloAbiertas, desde, hasta);
    Consulta consultaSeleccionada = null;

    if (consultaId != null) {
        try {
            consultaSeleccionada = consultaService.obtenerConsultaAdmin(consultaId);
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
        }
    } else if (!consultas.isEmpty()) {
        consultaSeleccionada = consultas.get(0);
    }

    if (consultaSeleccionada != null) {
        consultaService.marcarLeidaPorAdmin(consultaSeleccionada.getId());
    }

    List<MensajeConsulta> mensajes = consultaSeleccionada == null
        ? List.of()
        : consultaService.listarMensajes(consultaSeleccionada);

    List<DireccionEntrega> direcciones = consultaSeleccionada == null
        ? List.of()
        : direccionService.listarPorUsuario(consultaSeleccionada.getCliente());

    if (!model.containsAttribute("consultaMensajeRequest")) {
        model.addAttribute("consultaMensajeRequest", new ConsultaMensajeRequest());
    }
    if (!model.containsAttribute("pedidoManualRequest")) {
        PedidoManualDesdeConsultaRequest request = new PedidoManualDesdeConsultaRequest();
        request.setCantidad(1);
        model.addAttribute("pedidoManualRequest", request);
    }

    Set<Long> consultasNoLeidas = consultaService.obtenerIdsNoLeidosAdmin(consultas);

    model.addAttribute("usuario", usuario);
    model.addAttribute("consultas", consultas);
    model.addAttribute("consultaSeleccionada", consultaSeleccionada);
    model.addAttribute("mensajes", mensajes);
    model.addAttribute("direccionesCliente", direcciones);
    model.addAttribute("productosActivos", productoService.listarActivos());
    model.addAttribute("consultasNoLeidas", consultasNoLeidas);
    model.addAttribute("filtroCliente", cliente == null ? "" : cliente);
    model.addAttribute("filtroEstado", estado == null ? "todas" : estado);
    model.addAttribute("filtroDesde", desde);
    model.addAttribute("filtroHasta", hasta);
    return "admin/consultas";
}

@PostMapping("/consultas/{consultaId}/mensajes")
public String responderConsulta(
    HttpSession session,
    @PathVariable Long consultaId,
    @Valid @ModelAttribute("consultaMensajeRequest") ConsultaMensajeRequest request,
    BindingResult bindingResult,
    RedirectAttributes redirectAttributes
) {
    Usuario usuario = adminAutenticado(session);
    if (usuario == null) {
        return "redirect:/login";
    }

    if (bindingResult.hasErrors()) {
        redirectAttributes.addFlashAttribute("error", "Debes escribir un mensaje antes de enviarlo.");
        return "redirect:/admin/consultas?consultaId=" + consultaId;
    }

    try {
        consultaService.agregarMensajeAdmin(consultaId, usuario, request.getMensaje());
        return "redirect:/admin/consultas?consultaId=" + consultaId;
    } catch (BusinessException ex) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/consultas?consultaId=" + consultaId;
    }
}

@PostMapping("/consultas/{consultaId}/pedido-manual")
public String crearPedidoManualDesdeConsulta(
    HttpSession session,
    @PathVariable Long consultaId,
    @Valid @ModelAttribute("pedidoManualRequest") PedidoManualDesdeConsultaRequest request,
    BindingResult bindingResult,
    RedirectAttributes redirectAttributes
) {
    Usuario usuario = adminAutenticado(session);
    if (usuario == null) {
        return "redirect:/login";
    }

    if (bindingResult.hasErrors()) {
        redirectAttributes.addFlashAttribute("error", "Revisa los datos del pedido manual e intenta de nuevo.");
        return "redirect:/admin/consultas?consultaId=" + consultaId;
    }

    try {
        Pedido pedido = consultaService.crearPedidoManualDesdeConsulta(
            consultaId,
            usuario,
            request.getProductoId(),
            request.getCantidad(),
            request.getDireccionId(),
            request.getObservaciones()
        );
        redirectAttributes.addFlashAttribute("success", "Pedido manual #" + pedido.getId() + " creado correctamente.");
        return "redirect:/admin/consultas?consultaId=" + consultaId;
    } catch (BusinessException ex) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/consultas?consultaId=" + consultaId;
    }
}

@PostMapping("/consultas/{consultaId}/cerrar")
public String cerrarConsulta(
    HttpSession session,
    @PathVariable Long consultaId,
    RedirectAttributes redirectAttributes
) {
    Usuario usuario = adminAutenticado(session);
    if (usuario == null) {
        return "redirect:/login";
    }

    try {
        consultaService.cerrarConsultaAdmin(consultaId);
        redirectAttributes.addFlashAttribute("success", "Consulta cerrada correctamente.");
    } catch (BusinessException ex) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/consultas?consultaId=" + consultaId;
}

@PostMapping("/consultas/{consultaId}/reabrir")
public String reabrirConsulta(
    HttpSession session,
    @PathVariable Long consultaId,
    RedirectAttributes redirectAttributes
) {
    Usuario usuario = adminAutenticado(session);
    if (usuario == null) {
        return "redirect:/login";
    }

    try {
        consultaService.reabrirConsultaAdmin(consultaId);
        redirectAttributes.addFlashAttribute("success", "Consulta reabierta correctamente.");
    } catch (BusinessException ex) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/consultas?consultaId=" + consultaId;
}

    private Usuario adminAutenticado(HttpSession session) {
        if (!sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return null;
        }

        return sessionService.obtenerUsuario(session);
    }
}
