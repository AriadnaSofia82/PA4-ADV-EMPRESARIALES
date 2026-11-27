package com.isil.edu.pe.spedidos.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos necesarios para registrar una revisión.
 */
public class PedidoRevisionRequest {

    @NotNull(message = "El identificador del pedido es obligatorio.")
    @Positive(message = "El identificador del pedido debe ser positivo.")
    private Long pedidoId;

    @NotBlank(message = "La observación es obligatoria.")
    @Size(max = 500, message = "La observación no puede superar los 500 caracteres.")
    private String observacion;

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
