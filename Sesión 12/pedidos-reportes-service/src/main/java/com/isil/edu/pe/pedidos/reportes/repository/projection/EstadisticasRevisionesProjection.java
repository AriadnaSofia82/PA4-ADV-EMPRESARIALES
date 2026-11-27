package com.isil.edu.pe.pedidos.reportes.repository.projection;

import java.math.BigDecimal;

public record EstadisticasRevisionesProjection(
        long totalPedidos,
        long totalRevisados,
        long totalPendientes,
        BigDecimal porcentajeRevisado
) {
}