package com.Proyecto.GlaciarGestion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.GlaciarGestion.dto.LoginRequest;
import com.Proyecto.GlaciarGestion.dto.RegistroRequest;
import com.Proyecto.GlaciarGestion.model.RolUsuario;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario registrarCliente(RegistroRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BusinessException("El correo ya se encuentra registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(request.getCorreo().trim().toLowerCase());
        usuario.setTelefono(request.getTelefono().trim());
        usuario.setContrasena(request.getContrasena().trim());
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo().trim().toLowerCase())
            .orElseThrow(() -> new BusinessException("Credenciales incorrectas."));

        if (!usuario.isActivo()) {
            throw new BusinessException("Usuario inactivo.");
        }

        if (!usuario.getContrasena().equals(request.getContrasena())) {
            throw new BusinessException("Credenciales incorrectas.");
        }

        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
    }
}
