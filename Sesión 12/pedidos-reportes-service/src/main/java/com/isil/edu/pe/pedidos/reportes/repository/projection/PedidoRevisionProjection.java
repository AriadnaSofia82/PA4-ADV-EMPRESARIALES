package com.isil.edu.pe.pedidos.reportes.repository.projection;

import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
import java.time.LocalDateTime;

public record PedidoRevisionProjection(
        Pedido pedido,
        String observacion,
        LocalDateTime fechaRevision
) {
}