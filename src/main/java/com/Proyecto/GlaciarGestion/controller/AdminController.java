package com.Proyecto.GlaciarGestion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Proyecto.GlaciarGestion.model.EstadoPedido;
import com.Proyecto.GlaciarGestion.model.Producto;
import com.Proyecto.GlaciarGestion.model.RolUsuario;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.service.BusinessException;
import com.Proyecto.GlaciarGestion.service.PedidoService;
import com.Proyecto.GlaciarGestion.service.ProductoService;
import com.Proyecto.GlaciarGestion.web.SessionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final SessionService sessionService;
    private final ProductoService productoService;
    private final PedidoService pedidoService;

    public AdminController(SessionService sessionService, ProductoService productoService, PedidoService pedidoService) {
        this.sessionService = sessionService;
        this.productoService = productoService;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/productos")
    public String listarProductos(HttpSession session, Model model) {
        if (!sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return "redirect:/login";
        }

        Usuario usuario = sessionService.obtenerUsuario(session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("editando", false);
        if (!model.containsAttribute("producto")) {
            model.addAttribute("producto", new Producto());
        }
        return "admin/productos";
    }

    @PostMapping("/productos")
    public String registrarProducto(HttpSession session, @ModelAttribute("producto") Producto producto, Model model) {
        if (!sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return "redirect:/login";
        }

        try {
            producto.setActivo(true);
            productoService.guardar(producto);
            return "redirect:/admin/productos";
        } catch (BusinessException ex) {
            Usuario usuario = sessionService.obtenerUsuario(session);
            model.addAttribute("usuario", usuario);
            model.addAttribute("productos", productoService.listarTodos());
            model.addAttribute("editando", false);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("producto", producto);
            return "admin/productos";
        }
    }

    @GetMapping("/productos/{productoId}/editar")
    public String editarProducto(HttpSession session, @PathVariable Long productoId, Model model) {
        if (!sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return "redirect:/login";
        }

        Usuario usuario = sessionService.obtenerUsuario(session);
        Producto producto = productoService.listarTodos().stream()
            .filter(item -> item.getId().equals(productoId))
            .findFirst()
            .orElseThrow(() -> new BusinessException("Producto no encontrado."));

        model.addAttribute("usuario", usuario);
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("editando", true);
        model.addAttribute("producto", producto);
        return "admin/productos";
    }

    @PostMapping("/productos/{productoId}/editar")
    public String actualizarProducto(HttpSession session, @PathVariable Long productoId, @ModelAttribute("producto") Producto producto, Model model) {
        if (!sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return "redirect:/login";
        }

        try {
            productoService.actualizar(productoId, producto);
            return "redirect:/admin/productos";
        } catch (BusinessException ex) {
            Usuario usuario = sessionService.obtenerUsuario(session);
            model.addAttribute("usuario", usuario);
            model.addAttribute("productos", productoService.listarTodos());
            model.addAttribute("editando", true);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("producto", producto);
            return "admin/productos";
        }
    }

    @PostMapping("/productos/{productoId}/desactivar")
    public String desactivarProducto(HttpSession session, @PathVariable Long productoId) {
        if (!sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return "redirect:/login";
        }

        productoService.desactivar(productoId);
        return "redirect:/admin/productos";
    }

    @GetMapping("/pedidos")
    public String listarPedidos(HttpSession session, Model model) {
        if (!sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return "redirect:/login";
        }

        Usuario usuario = sessionService.obtenerUsuario(session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("pedidos", pedidoService.listarTodos());
        model.addAttribute("estados", EstadoPedido.values());
        return "admin/pedidos";
    }

    @PostMapping("/pedidos/{pedidoId}/estado")
    public String actualizarEstadoPedido(HttpSession session, @PathVariable Long pedidoId, @RequestParam("estado") EstadoPedido estado) {
        if (!sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return "redirect:/login";
        }

        pedidoService.actualizarEstadoPedido(pedidoId, estado);
        return "redirect:/admin/pedidos";
    }
}
