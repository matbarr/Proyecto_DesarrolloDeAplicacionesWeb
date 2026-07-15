package com.Proyecto.GlaciarGestion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.model.DireccionEntrega;
import com.Proyecto.GlaciarGestion.model.Usuario;

public interface DireccionEntregaRepository extends JpaRepository<DireccionEntrega, Long> {
    List<DireccionEntrega> findByUsuarioOrderByIdDesc(Usuario usuario);
}
