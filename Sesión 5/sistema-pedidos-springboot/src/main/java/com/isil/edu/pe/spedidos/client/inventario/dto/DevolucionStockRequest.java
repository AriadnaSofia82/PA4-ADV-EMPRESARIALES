package com.isil.edu.pe.spedidos.client.inventario.dto;

/** DTO enviado a inventario-service para devolver unidades al stock. */
public record DevolucionStockRequest(int cantidad) {
}
