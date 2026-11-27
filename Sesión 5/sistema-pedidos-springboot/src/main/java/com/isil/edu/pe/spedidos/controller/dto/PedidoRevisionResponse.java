package com.isil.edu.pe.spedidos.controller.dto;

import java.time.LocalDateTime;

/**
 * Respuesta generada después de registrar una revisión.
 */
public class PedidoRevisionResponse {

    private Long id;

    private Long pedidoId;

    private String observacion;

    private LocalDateTime fechaRevision;

    public PedidoRevisionResponse() {
    }

    public PedidoRevisionResponse(
            Long id,
            Long pedidoId,
            String observacion,
            LocalDateTime fechaRevision
    ) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.observacion = observacion;
        this.fechaRevision = fechaRevision;
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public String getObservacion() {
        return observacion;
    }

    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public void setFechaRevision(LocalDateTime fechaRevision) {
        this.fechaRevision = fechaRevision;
    }
}
