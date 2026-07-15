package com.Proyecto.GlaciarGestion.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.GlaciarGestion.dto.DireccionRequest;
import com.Proyecto.GlaciarGestion.model.DireccionEntrega;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.repository.DireccionEntregaRepository;

@Service
public class DireccionService {

    private final DireccionEntregaRepository direccionEntregaRepository;

    public DireccionService(DireccionEntregaRepository direccionEntregaRepository) {
        this.direccionEntregaRepository = direccionEntregaRepository;
    }

    @Transactional
    public DireccionEntrega registrar(Usuario usuario, DireccionRequest request) {
        DireccionEntrega direccion = new DireccionEntrega();
        direccion.setUsuario(usuario);
        direccion.setProvincia(request.getProvincia().trim());
        direccion.setCanton(request.getCanton().trim());
        direccion.setDistrito(request.getDistrito().trim());
        direccion.setDireccionExacta(request.getDireccionExacta().trim());
        return direccionEntregaRepository.save(direccion);
    }

    @Transactional(readOnly = true)
    public List<DireccionEntrega> listarPorUsuario(Usuario usuario) {
        return direccionEntregaRepository.findByUsuarioOrderByIdDesc(usuario);
    }

    @Transactional(readOnly = true)
    public DireccionEntrega obtenerDireccionDelUsuario(Long direccionId, Usuario usuario) {
        DireccionEntrega direccion = direccionEntregaRepository.findById(direccionId)
            .orElseThrow(() -> new BusinessException("Direccion no encontrada."));

        if (!direccion.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessException("La direccion no pertenece al cliente autenticado.");
        }

        return direccion;
    }
}
