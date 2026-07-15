package com.Proyecto.GlaciarGestion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.model.Carrito;
import com.Proyecto.GlaciarGestion.model.DetalleCarrito;
import com.Proyecto.GlaciarGestion.model.Producto;

public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {
    List<DetalleCarrito> findByCarritoOrderByIdAsc(Carrito carrito);

    Optional<DetalleCarrito> findByCarritoAndProducto(Carrito carrito, Producto producto);

    void deleteByCarrito(Carrito carrito);
}
