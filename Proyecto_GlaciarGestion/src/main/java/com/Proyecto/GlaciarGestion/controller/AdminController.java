package com.Proyecto.GlaciarGestion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Proyecto.GlaciarGestion.dto.ProductoRequest;
import com.Proyecto.GlaciarGestion.model.Producto;
import com.Proyecto.GlaciarGestion.model.RolUsuario;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.service.BusinessException;
import com.Proyecto.GlaciarGestion.service.ProductoService;
import com.Proyecto.GlaciarGestion.web.SessionService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final SessionService sessionService;
    private final ProductoService productoService;

    public AdminController(SessionService sessionService, ProductoService productoService) {
        this.sessionService = sessionService;
        this.productoService = productoService;
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

        productoService.registrarProducto(productoRequest);
        return "redirect:/admin/productos";
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

        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("producto", productoService.obtenerPorId(productoId));
            model.addAttribute("modo", "editar");
            return "admin/producto-form";
        }

        productoService.actualizarProducto(productoId, productoRequest);
        return "redirect:/admin/productos";
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

    private Usuario adminAutenticado(HttpSession session) {
        if (!sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return null;
        }

        return sessionService.obtenerUsuario(session);
    }
}