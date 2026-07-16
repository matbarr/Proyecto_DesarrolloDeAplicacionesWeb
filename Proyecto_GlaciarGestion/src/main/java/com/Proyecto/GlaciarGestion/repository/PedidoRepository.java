package com.Proyecto.GlaciarGestion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.model.Pedido;
import com.Proyecto.GlaciarGestion.model.Usuario;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioOrderByFechaDesc(Usuario usuario);
    
    List<Pedido> findAllByOrderByFechaDesc();
}
