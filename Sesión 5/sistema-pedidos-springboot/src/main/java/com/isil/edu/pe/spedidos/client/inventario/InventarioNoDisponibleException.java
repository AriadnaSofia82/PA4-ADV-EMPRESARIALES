package com.isil.edu.pe.spedidos.client.inventario;

public class InventarioNoDisponibleException extends RuntimeException {

  public InventarioNoDisponibleException(String message) {
    super(message);
  }

  public InventarioNoDisponibleException(String message, Throwable cause) {
    super(message, cause);
  }
}
