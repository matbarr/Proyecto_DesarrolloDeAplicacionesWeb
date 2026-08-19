package com.Proyecto.GlaciarGestion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.domain.Consulta;
import com.Proyecto.GlaciarGestion.domain.Usuario;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByClienteOrderByFechaActualizacionDesc(Usuario cliente);

    List<Consulta> findAllByOrderByFechaActualizacionDesc();
}
