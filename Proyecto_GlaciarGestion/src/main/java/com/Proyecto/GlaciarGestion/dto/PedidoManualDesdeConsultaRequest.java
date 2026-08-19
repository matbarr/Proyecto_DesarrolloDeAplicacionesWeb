package com.Proyecto.GlaciarGestion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PedidoManualDesdeConsultaRequest {

    @NotNull(message = "El producto es obligatorio.")
    private Long productoId;

    @NotNull(message = "La direccion es obligatoria.")
    private Long direccionId;

    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad debe ser al menos 1.")
    private Integer cantidad;

    @Size(max = 600, message = "Las observaciones no pueden superar 600 caracteres.")
    private String observaciones;

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Long getDireccionId() {
        return direccionId;
    }

    public void setDireccionId(Long direccionId) {
        this.direccionId = direccionId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
