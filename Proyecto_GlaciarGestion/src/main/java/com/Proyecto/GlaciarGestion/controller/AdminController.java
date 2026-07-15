package com.Proyecto.GlaciarGestion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Proyecto.GlaciarGestion.model.RolUsuario;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.service.ProductoService;
import com.Proyecto.GlaciarGestion.web.SessionService;

import jakarta.servlet.http.HttpSession;

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
        if (!sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return "redirect:/login";
        }

        Usuario usuario = sessionService.obtenerUsuario(session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("productos", productoService.listarTodos());
        return "admin/productos";
    }
}
