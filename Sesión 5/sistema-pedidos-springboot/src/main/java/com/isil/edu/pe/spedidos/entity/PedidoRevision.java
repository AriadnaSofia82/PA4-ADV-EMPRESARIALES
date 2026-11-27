package com.isil.edu.pe.spedidos.entity;

import java.time.LocalDateTime;

/**
 * Representa la revisión realizada sobre un pedido.
 *
 * Esta clase se utiliza mediante JDBC.
 */
public class PedidoRevision {

    private Long id;

    private Long pedidoId;

    private String observacion;

    private LocalDateTime fechaRevision;

    public PedidoRevision() {
    }

    public PedidoRevision(
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

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(LocalDateTime fechaRevision) {
        this.fechaRevision = fechaRevision;
    }
}
