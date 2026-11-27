package com.isil.edu.pe.spedidos.exception;

public class PedidoRevisionException extends RuntimeException {

    public PedidoRevisionException(String message) {
        super(message);
    }

    public PedidoRevisionException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
