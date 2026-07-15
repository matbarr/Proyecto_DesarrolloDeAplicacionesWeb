package com.Proyecto.GlaciarGestion.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CarritoResumenView {

    private List<CarritoItemView> items = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;

    public List<CarritoItemView> getItems() {
        return items;
    }

    public void setItems(List<CarritoItemView> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
