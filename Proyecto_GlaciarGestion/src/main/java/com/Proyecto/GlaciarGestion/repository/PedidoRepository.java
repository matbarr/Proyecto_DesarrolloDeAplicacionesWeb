package com.Proyecto.GlaciarGestion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.domain.Pedido;
import com.Proyecto.GlaciarGestion.domain.Usuario;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioOrderByFechaDesc(Usuario usuario);
    
    List<Pedido> findAllByOrderByFechaDesc();
}

