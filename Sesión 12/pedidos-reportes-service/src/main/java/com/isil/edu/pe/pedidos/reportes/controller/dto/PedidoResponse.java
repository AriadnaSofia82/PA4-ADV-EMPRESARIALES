package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResponse(
        Long id,
        String cliente,
        String productoCodigo,
        String productoNombre,
        BigDecimal precioUnitario,
        int cantidad,
        BigDecimal total,
        LocalDateTime fecha,
        String estado,
        boolean revisado
) {

  public static PedidoResponse from(Pedido pedido) {
    return new PedidoResponse(
            pedido.id(),
            pedido.cliente(),
            pedido.productoCodigo(),
            pedido.productoNombre(),
            pedido.precioUnitario(),
            pedido.cantidad(),
            pedido.total(),
            pedido.fecha(),
            pedido.estado(),
            pedido.revisado()
    );
  }
}
