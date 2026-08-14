package com.Proyecto.GlaciarGestion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.domain.Carrito;
import com.Proyecto.GlaciarGestion.domain.Usuario;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByUsuario(Usuario usuario);
}

