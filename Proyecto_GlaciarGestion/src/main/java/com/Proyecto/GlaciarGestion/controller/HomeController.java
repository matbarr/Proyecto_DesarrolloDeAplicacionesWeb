package com.Proyecto.GlaciarGestion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.Proyecto.GlaciarGestion.model.RolUsuario;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.web.SessionService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final SessionService sessionService;

    public HomeController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/")
    public String inicio(HttpSession session) {
        Usuario usuario = sessionService.obtenerUsuario(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        if (sessionService.tieneRol(session, RolUsuario.ADMINISTRADOR)) {
            return "redirect:/admin/productos";
        }

        return "redirect:/cliente/productos";
    }
}
