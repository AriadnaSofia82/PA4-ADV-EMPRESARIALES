package com.isil.edu.pe.pedidos.reportes.controller;
import com.isil.edu.pe.pedidos.reportes.controller.dto.PedidoResponse;
import com.isil.edu.pe.pedidos.reportes.service.PedidoReporteService;
import com.isil.edu.pe.pedidos.reportes.service.domain.ResumenPedidos;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class PedidoApiController {

  private final PedidoReporteService pedidoReporteService;

  public PedidoApiController(
          PedidoReporteService pedidoReporteService) {
    this.pedidoReporteService = pedidoReporteService;
  }

  @GetMapping
  public List<PedidoResponse> listarPedidos() {
    return pedidoReporteService.listarTodos()
            .stream()
            .map(PedidoResponse::from)
            .toList();
  }

  @GetMapping(
          value = "/resumen",
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Map<String, Object> resumenPedidos() {

    ResumenPedidos resumen =
            pedidoReporteService.obtenerResumen();

    Map<String, Object> resultado = new HashMap<>();

    resultado.put("modulo", "Sistema de Pedidos");
    resultado.put("totalPedidos", resumen.totalPedidos());
    resultado.put("totalUnidades", resumen.totalUnidades());

    return resultado;
  }
}
