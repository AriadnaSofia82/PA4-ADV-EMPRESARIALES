package com.isil.edu.pe.pedidos.reportes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(PedidoNoEncontradoException.class)
  public ProblemDetail pedidoNoEncontrado(PedidoNoEncontradoException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            e.getMessage()
    );

    problem.setTitle("Pedido no encontrado");
    return problem;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "El parámetro '" + e.getName() + "' tiene un formato inválido."
    );

    problem.setTitle("Parámetro inválido");
    return problem;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail errorValidacion(MethodArgumentNotValidException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Uno o más campos contienen valores inválidos."
    );

    problem.setTitle("Error de validación");
    return problem;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail argumentoInvalido(IllegalArgumentException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            e.getMessage()
    );

    problem.setTitle("Solicitud inválida");
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail errorInterno(Exception e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocurrió un error interno al procesar la solicitud."
    );

    problem.setTitle("Error interno del servidor");
    return problem;
  }
}