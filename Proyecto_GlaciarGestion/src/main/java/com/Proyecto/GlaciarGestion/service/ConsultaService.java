package com.Proyecto.GlaciarGestion.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Proyecto.GlaciarGestion.domain.Consulta;
import com.Proyecto.GlaciarGestion.domain.MensajeConsulta;
import com.Proyecto.GlaciarGestion.domain.Pedido;
import com.Proyecto.GlaciarGestion.domain.RolUsuario;
import com.Proyecto.GlaciarGestion.domain.Usuario;
import com.Proyecto.GlaciarGestion.repository.ConsultaRepository;
import com.Proyecto.GlaciarGestion.repository.MensajeConsultaRepository;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final MensajeConsultaRepository mensajeConsultaRepository;
    private final PedidoService pedidoService;

    public ConsultaService(
        ConsultaRepository consultaRepository,
        MensajeConsultaRepository mensajeConsultaRepository,
        PedidoService pedidoService
    ) {
        this.consultaRepository = consultaRepository;
        this.mensajeConsultaRepository = mensajeConsultaRepository;
        this.pedidoService = pedidoService;
    }

    @Transactional(readOnly = true)
    public List<Consulta> listarConsultasCliente(Usuario cliente) {
        return consultaRepository.findByClienteOrderByFechaActualizacionDesc(cliente);
    }

    @Transactional(readOnly = true)
    public List<Consulta> listarConsultasAdmin() {
        return consultaRepository.findAllByOrderByFechaActualizacionDesc();
    }

    @Transactional(readOnly = true)
    public List<Consulta> listarConsultasAdminFiltradas(
        String clienteFiltro,
        Boolean soloAbiertas,
        LocalDate desde,
        LocalDate hasta
    ) {
        String filtro = clienteFiltro == null ? "" : clienteFiltro.trim().toLowerCase();

        return consultaRepository.findAllByOrderByFechaActualizacionDesc().stream()
            .filter(consulta -> filtro.isEmpty() || coincideCliente(consulta, filtro))
            .filter(consulta -> soloAbiertas == null || consulta.isAbierta() == soloAbiertas)
            .filter(consulta -> desde == null || !consulta.getFechaCreacion().toLocalDate().isBefore(desde))
            .filter(consulta -> hasta == null || !consulta.getFechaCreacion().toLocalDate().isAfter(hasta))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Set<Long> obtenerIdsNoLeidosCliente(Usuario cliente) {
        return listarConsultasCliente(cliente).stream()
            .filter(consulta -> consulta.getUltimoLeidoCliente() == null
                || consulta.getFechaActualizacion().isAfter(consulta.getUltimoLeidoCliente()))
            .map(Consulta::getId)
            .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<Long> obtenerIdsNoLeidosAdmin(List<Consulta> consultas) {
        return consultas.stream()
            .filter(consulta -> consulta.getUltimoLeidoAdmin() == null
                || consulta.getFechaActualizacion().isAfter(consulta.getUltimoLeidoAdmin()))
            .map(Consulta::getId)
            .collect(Collectors.toSet());
    }

    @Transactional
    public Consulta crearConsulta(Usuario cliente, String asunto, String mensajeInicial) {
        String asuntoLimpio = limpiarTexto(asunto, "El asunto es obligatorio.");
        String mensajeLimpio = limpiarTexto(mensajeInicial, "El mensaje inicial es obligatorio.");

        Consulta consulta = new Consulta();
        consulta.setCliente(cliente);
        consulta.setAsunto(asuntoLimpio);
        consulta.setAbierta(true);
        consulta.setFechaCreacion(LocalDateTime.now());
        consulta.setFechaActualizacion(LocalDateTime.now());
        consulta.setUltimoLeidoCliente(LocalDateTime.now());
        consulta.setUltimoLeidoAdmin(null);

        Consulta guardada = consultaRepository.save(consulta);
        crearMensaje(guardada, cliente, mensajeLimpio);
        return guardada;
    }

    @Transactional(readOnly = true)
    public Consulta obtenerConsultaCliente(Long consultaId, Usuario cliente) {
        Consulta consulta = obtenerConsultaAdmin(consultaId);
        if (!consulta.getCliente().getId().equals(cliente.getId())) {
            throw new BusinessException("La consulta no pertenece al cliente autenticado.");
        }
        return consulta;
    }

    @Transactional(readOnly = true)
    public Consulta obtenerConsultaAdmin(Long consultaId) {
        return consultaRepository.findById(consultaId)
            .orElseThrow(() -> new BusinessException("Consulta no encontrada."));
    }

    @Transactional(readOnly = true)
    public List<MensajeConsulta> listarMensajes(Consulta consulta) {
        return mensajeConsultaRepository.findByConsultaOrderByFechaAsc(consulta);
    }

    @Transactional
    public void agregarMensajeCliente(Long consultaId, Usuario cliente, String mensaje) {
        Consulta consulta = obtenerConsultaCliente(consultaId, cliente);
        if (!consulta.isAbierta()) {
            throw new BusinessException("La consulta esta cerrada. Reabrela para continuar.");
        }
        crearMensaje(consulta, cliente, limpiarTexto(mensaje, "El mensaje no puede estar vacio."));
    }

    @Transactional
    public void agregarMensajeAdmin(Long consultaId, Usuario admin, String mensaje) {
        Consulta consulta = obtenerConsultaAdmin(consultaId);
        if (!consulta.isAbierta()) {
            throw new BusinessException("La consulta esta cerrada. Reabrela para continuar.");
        }
        crearMensaje(consulta, admin, limpiarTexto(mensaje, "El mensaje no puede estar vacio."));
    }

    @Transactional
    public void cerrarConsultaCliente(Long consultaId, Usuario cliente) {
        Consulta consulta = obtenerConsultaCliente(consultaId, cliente);
        consulta.setAbierta(false);
        consulta.setFechaActualizacion(LocalDateTime.now());
        consultaRepository.save(consulta);
    }

    @Transactional
    public void reabrirConsultaCliente(Long consultaId, Usuario cliente) {
        Consulta consulta = obtenerConsultaCliente(consultaId, cliente);
        consulta.setAbierta(true);
        consulta.setFechaActualizacion(LocalDateTime.now());
        consultaRepository.save(consulta);
    }

    @Transactional
    public void cerrarConsultaAdmin(Long consultaId) {
        Consulta consulta = obtenerConsultaAdmin(consultaId);
        consulta.setAbierta(false);
        consulta.setFechaActualizacion(LocalDateTime.now());
        consultaRepository.save(consulta);
    }

    @Transactional
    public void reabrirConsultaAdmin(Long consultaId) {
        Consulta consulta = obtenerConsultaAdmin(consultaId);
        consulta.setAbierta(true);
        consulta.setFechaActualizacion(LocalDateTime.now());
        consultaRepository.save(consulta);
    }

    @Transactional
    public void marcarLeidaPorCliente(Long consultaId, Usuario cliente) {
        Consulta consulta = obtenerConsultaCliente(consultaId, cliente);
        consulta.setUltimoLeidoCliente(LocalDateTime.now());
        consultaRepository.save(consulta);
    }

    @Transactional
    public void marcarLeidaPorAdmin(Long consultaId) {
        Consulta consulta = obtenerConsultaAdmin(consultaId);
        consulta.setUltimoLeidoAdmin(LocalDateTime.now());
        consultaRepository.save(consulta);
    }

    @Transactional
    public Pedido crearPedidoManualDesdeConsulta(
        Long consultaId,
        Usuario admin,
        Long productoId,
        Integer cantidad,
        Long direccionId,
        String observaciones
    ) {
        Consulta consulta = obtenerConsultaAdmin(consultaId);
        Pedido pedido = pedidoService.crearPedidoManualAdmin(
            consulta.getCliente(),
            productoId,
            cantidad,
            direccionId,
            observaciones
        );

        StringBuilder resumen = new StringBuilder();
        resumen.append("Pedido manual creado por ")
            .append(admin.getNombre())
            .append(". Numero de pedido: #")
            .append(pedido.getId())
            .append(". Estado: ")
            .append(pedido.getEstado());

        crearMensaje(consulta, admin, resumen.toString());
        return pedido;
    }

    private void crearMensaje(Consulta consulta, Usuario emisor, String contenido) {
        LocalDateTime ahora = LocalDateTime.now();

        MensajeConsulta mensaje = new MensajeConsulta();
        mensaje.setConsulta(consulta);
        mensaje.setEmisor(emisor);
        mensaje.setContenido(contenido);
        mensaje.setFecha(ahora);
        mensajeConsultaRepository.save(mensaje);

        consulta.setFechaActualizacion(ahora);
        if (emisor.getRol() == RolUsuario.CLIENTE) {
            consulta.setUltimoLeidoCliente(ahora);
        } else if (emisor.getRol() == RolUsuario.ADMINISTRADOR) {
            consulta.setUltimoLeidoAdmin(ahora);
        }
        consultaRepository.save(consulta);
    }

    private boolean coincideCliente(Consulta consulta, String filtro) {
        String nombre = consulta.getCliente().getNombre() == null ? "" : consulta.getCliente().getNombre().toLowerCase();
        String correo = consulta.getCliente().getCorreo() == null ? "" : consulta.getCliente().getCorreo().toLowerCase();
        return nombre.contains(filtro) || correo.contains(filtro);
    }

    private String limpiarTexto(String texto, String errorMessage) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new BusinessException(errorMessage);
        }
        return texto.trim();
    }
}
