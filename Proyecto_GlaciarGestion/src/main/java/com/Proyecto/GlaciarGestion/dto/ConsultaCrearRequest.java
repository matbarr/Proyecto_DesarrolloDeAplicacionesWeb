package com.Proyecto.GlaciarGestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ConsultaCrearRequest {

    @NotBlank(message = "El asunto es obligatorio.")
    @Size(max = 120, message = "El asunto no puede superar 120 caracteres.")
    private String asunto;

    @NotBlank(message = "El mensaje inicial es obligatorio.")
    @Size(max = 2000, message = "El mensaje no puede superar 2000 caracteres.")
    private String mensaje;

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
