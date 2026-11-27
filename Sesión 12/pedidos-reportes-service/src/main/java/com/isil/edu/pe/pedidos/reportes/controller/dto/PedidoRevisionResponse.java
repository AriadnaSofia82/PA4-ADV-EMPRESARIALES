package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevision;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoRevisionResponse(
        Long id,
        String cliente,
        String productoCodigo,
        String productoNombre,
        BigDecimal precioUnitario,
        int cantidad,
        BigDecimal total,
        LocalDateTime fecha,
        String estado,
        boolean revisado,
        String observacion,
        LocalDateTime fechaRevision
) {

    public static PedidoRevisionResponse from(PedidoRevision revision) {
        var pedido = revision.pedido();

        return new PedidoRevisionResponse(
                pedido.id(),
                pedido.cliente(),
                pedido.productoCodigo(),
                pedido.productoNombre(),
                pedido.precioUnitario(),
                pedido.cantidad(),
                pedido.total(),
                pedido.fecha(),
                pedido.estado(),
                pedido.revisado(),
                revision.observacion(),
                revision.fechaRevision()
        );
    }
}