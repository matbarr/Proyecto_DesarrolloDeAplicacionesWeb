package com.Proyecto.GlaciarGestion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Proyecto.GlaciarGestion.dto.LoginRequest;
import com.Proyecto.GlaciarGestion.dto.RegistroRequest;
import com.Proyecto.GlaciarGestion.model.RolUsuario;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.service.AuthService;
import com.Proyecto.GlaciarGestion.service.BusinessException;
import com.Proyecto.GlaciarGestion.web.SessionService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;

    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @GetMapping("/login")
    public String loginView(Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
        @Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
        BindingResult bindingResult,
        HttpSession session,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            Usuario usuario = authService.autenticar(loginRequest);
            sessionService.iniciarSesion(session, usuario);
            if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
                return "redirect:/admin/productos";
            }
            return "redirect:/cliente/productos";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login";
        }
    }

    @GetMapping("/registro")
    public String registroView(Model model) {
        if (!model.containsAttribute("registroRequest")) {
            model.addAttribute("registroRequest", new RegistroRequest());
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
        @Valid @ModelAttribute("registroRequest") RegistroRequest registroRequest,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "registro";
        }

        try {
            authService.registrarCliente(registroRequest);
            redirectAttributes.addFlashAttribute("success", "Registro exitoso. Ya puedes iniciar sesion.");
            return "redirect:/login";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            return "registro";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        sessionService.cerrarSesion(session);
        return "redirect:/login";
    }
}
