package com.Proyecto.GlaciarGestion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.domain.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrueOrderByNombreAsc();
}

