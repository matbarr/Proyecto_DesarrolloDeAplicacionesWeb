package com.Proyecto.GlaciarGestion.dto;

import jakarta.validation.constraints.NotBlank;

public class DireccionRequest {

    @NotBlank
    private String provincia;

    @NotBlank
    private String canton;

    @NotBlank
    private String distrito;

    @NotBlank
    private String direccionExacta;

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCanton() {
        return canton;
    }

    public void setCanton(String canton) {
        this.canton = canton;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getDireccionExacta() {
        return direccionExacta;
    }

    public void setDireccionExacta(String direccionExacta) {
        this.direccionExacta = direccionExacta;
    }
}
