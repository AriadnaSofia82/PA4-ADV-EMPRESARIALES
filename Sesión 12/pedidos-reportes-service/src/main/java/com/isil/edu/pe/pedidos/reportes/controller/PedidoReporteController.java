package com.isil.edu.pe.pedidos.reportes.controller;

import com.isil.edu.pe.pedidos.reportes.controller.dto.EstadisticasRevisionesDto;
import com.isil.edu.pe.pedidos.reportes.controller.dto.PedidoResponse;
import com.isil.edu.pe.pedidos.reportes.controller.dto.PedidoRevisionResponse;
import com.isil.edu.pe.pedidos.reportes.controller.dto.ProductoVendidoDto;
import com.isil.edu.pe.pedidos.reportes.controller.dto.ResumenPedidosDto;
import com.isil.edu.pe.pedidos.reportes.service.PedidoReporteService;
import com.isil.edu.pe.pedidos.reportes.service.domain.EstadoRevision;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para reportes de pedidos.
 *
 * Proporciona endpoints para consultar pedidos,
 * estadísticas, productos vendidos y trazabilidad de revisiones.
 */
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoReporteController {

  private final PedidoReporteService reporteService;

  public PedidoReporteController(PedidoReporteService reporteService) {
    this.reporteService = reporteService;
  }

  @GetMapping(
          value = "/{id}",
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  public PedidoResponse obtenerPorId(@PathVariable long id) {
    return PedidoResponse.from(reporteService.obtenerPorId(id));
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<PedidoResponse> listarPedidosPorCliente(
          @RequestParam String cliente) {

    return reporteService.buscarPorCliente(cliente)
            .stream()
            .map(PedidoResponse::from)
            .toList();
  }

  @GetMapping(
          value = "/estadisticas/resumen",
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<ResumenPedidosDto> obtenerResumen() {
    return ResponseEntity.ok(
            ResumenPedidosDto.from(reporteService.obtenerResumen())
    );
  }

  @GetMapping(
          value = "/estadisticas/productos",
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<ProductoVendidoDto> listarProductosMasVendidos() {

    return reporteService.listarProductosMasVendidos()
            .stream()
            .map(ProductoVendidoDto::from)
            .toList();
  }

  /**
   * Consulta pedidos pendientes o revisados.
   */
  @GetMapping(
          value = "/revisiones",
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  public List<PedidoRevisionResponse> listarPorEstadoRevision(
          @RequestParam EstadoRevision estado) {

    return reporteService.listarPorEstadoRevision(estado)
            .stream()
            .map(PedidoRevisionResponse::from)
            .toList();
  }

  /**
   * Consulta las estadísticas generales de revisión.
   */
  @GetMapping(
          value = "/estadisticas/revisiones",
          produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<EstadisticasRevisionesDto>
  obtenerEstadisticasRevisiones() {

    return ResponseEntity.ok(
            EstadisticasRevisionesDto.from(
                    reporteService.obtenerEstadisticasRevisiones()
            )
    );
  }
}