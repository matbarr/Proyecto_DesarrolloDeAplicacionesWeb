package com.Proyecto.GlaciarGestion.web;

import org.springframework.stereotype.Component;

import com.Proyecto.GlaciarGestion.model.RolUsuario;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.service.AuthService;

import jakarta.servlet.http.HttpSession;

@Component
public class SessionService {

    private static final String USER_ID_KEY = "USER_ID";
    private static final String USER_ROLE_KEY = "USER_ROLE";

    private final AuthService authService;

    public SessionService(AuthService authService) {
        this.authService = authService;
    }

    public void iniciarSesion(HttpSession session, Usuario usuario) {
        session.setAttribute(USER_ID_KEY, usuario.getId());
        session.setAttribute(USER_ROLE_KEY, usuario.getRol().name());
    }

    public void cerrarSesion(HttpSession session) {
        session.invalidate();
    }

    public Usuario obtenerUsuario(HttpSession session) {
        Object value = session.getAttribute(USER_ID_KEY);
        if (!(value instanceof Long)) {
            return null;
        }
        Long userId = (Long) value;
        return authService.obtenerPorId(userId);
    }

    public boolean tieneRol(HttpSession session, RolUsuario rol) {
        Object role = session.getAttribute(USER_ROLE_KEY);
        return role instanceof String && rol.name().equals(role);
    }
}
