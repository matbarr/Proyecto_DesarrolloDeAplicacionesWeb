package com.Proyecto.GlaciarGestion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.domain.Carrito;
import com.Proyecto.GlaciarGestion.domain.DetalleCarrito;
import com.Proyecto.GlaciarGestion.domain.Producto;

public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {
    List<DetalleCarrito> findByCarritoOrderByIdAsc(Carrito carrito);

    Optional<DetalleCarrito> findByCarritoAndProducto(Carrito carrito, Producto producto);

    void deleteByCarrito(Carrito carrito);
}

