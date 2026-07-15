package com.Proyecto.GlaciarGestion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.model.Carrito;
import com.Proyecto.GlaciarGestion.model.Usuario;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByUsuario(Usuario usuario);
}
