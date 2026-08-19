package com.Proyecto.GlaciarGestion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Proyecto.GlaciarGestion.domain.Consulta;
import com.Proyecto.GlaciarGestion.domain.MensajeConsulta;

public interface MensajeConsultaRepository extends JpaRepository<MensajeConsulta, Long> {
    List<MensajeConsulta> findByConsultaOrderByFechaAsc(Consulta consulta);
}
