package com.Proyecto.GlaciarGestion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.domain.DetallePedido;
import com.Proyecto.GlaciarGestion.domain.Pedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
    List<DetallePedido> findByPedidoOrderByIdAsc(Pedido pedido);
}

