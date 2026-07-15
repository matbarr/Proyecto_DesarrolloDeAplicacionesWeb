package com.Proyecto.GlaciarGestion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.model.DetallePedido;
import com.Proyecto.GlaciarGestion.model.Pedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
    List<DetallePedido> findByPedidoOrderByIdAsc(Pedido pedido);
}
