package com.isil.edu.pe.pedidos.reportes.service.domain;

import java.time.LocalDateTime;

public record PedidoRevision(
        Pedido pedido,
        String observacion,
        LocalDateTime fechaRevision
) {
}