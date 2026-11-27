package com.isil.edu.pe.spedidos.client.inventario;

import com.isil.edu.pe.spedidos.client.inventario.dto.ProductoInventarioResponse;
import com.isil.edu.pe.spedidos.client.inventario.dto.ReservaStockResponse;

import java.util.List;

public interface InventarioClient {

    List<ProductoInventarioResponse> listarProductos();

    ReservaStockResponse reservarStock(String codigo, int cantidad);
}
