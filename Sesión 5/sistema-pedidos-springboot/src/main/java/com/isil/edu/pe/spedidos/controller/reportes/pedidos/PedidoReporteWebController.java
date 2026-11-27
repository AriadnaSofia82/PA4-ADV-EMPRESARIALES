package com.isil.edu.pe.spedidos.controller.reportes.pedidos;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import com.isil.edu.pe.spedidos.service.domain.ClienteFrecuente;

import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reportes/pedidos")
public class PedidoReporteWebController {

    private final PedidoReporteClient pedidoReporteClient;
    private final PedidoReporteService reporteService;

    public PedidoReporteWebController(
            PedidoReporteClient pedidoReporteClient,
            PedidoReporteService reporteService) {

        this.pedidoReporteClient = pedidoReporteClient;
        this.reporteService = reporteService;
    }

    @GetMapping
    public String mostrarReportes(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) BigDecimal totalMinimo,
            @RequestParam(required = false) Integer minPedidos,
            @RequestParam(required = false) String estadoRevision,
            Model model,
            HttpServletResponse response) {

        String nombreCliente = cliente == null ? "" : cliente.trim();

        model.addAttribute("cliente", nombreCliente);
        model.addAttribute("clienteAvanzado", nombreCliente);
        model.addAttribute("totalMinimo", totalMinimo);
        model.addAttribute("minPedidos", minPedidos == null ? 2 : minPedidos);

        // ==========================================================
        // INICIALIZACIÓN DE LOS ATRIBUTOS
        // ==========================================================

        model.addAttribute("productos", List.of());
        model.addAttribute("pedidos", List.of());
        model.addAttribute("pedidosAvanzados", List.of());
        model.addAttribute("clientesFrecuentes", List.of());

        // PA04: revisión y trazabilidad
        model.addAttribute("revisiones", List.of());
        model.addAttribute("estadisticasRevisiones", null);
        model.addAttribute("estadoRevision", estadoRevision);

        model.addAttribute("busquedaSimpleRealizada", false);
        model.addAttribute("busquedaAvanzadaRealizada", false);
        model.addAttribute("frecuentesRealizado", false);
        model.addAttribute("busquedaRevisionRealizada", false);

        // ==========================================================
        // REPORTES GENERALES OBTENIDOS DESDE EL SERVICIO REST
        // ==========================================================

        try {

            model.addAttribute(
                    "resumen",
                    pedidoReporteClient.obtenerResumen()
            );

            model.addAttribute(
                    "productos",
                    pedidoReporteClient.listarProductosMasVendidos()
            );

        } catch (PedidoReporteNoDisponibleException exception) {

            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

            model.addAttribute(
                    "errorReporte",
                    exception.getMessage()
            );
        }

        // ==========================================================
        // BÚSQUEDA SIMPLE DE PEDIDOS POR CLIENTE
        // ==========================================================

        if (!nombreCliente.isBlank()
                && totalMinimo == null
                && minPedidos == null) {

            model.addAttribute("busquedaSimpleRealizada", true);

            try {

                model.addAttribute(
                        "pedidos",
                        pedidoReporteClient.buscarPorCliente(nombreCliente)
                );

            } catch (PedidoReporteNoDisponibleException exception) {

                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

                model.addAttribute(
                        "errorReporte",
                        exception.getMessage()
                );
            }
        }

        // ==========================================================
        // BÚSQUEDA AVANZADA LOCAL: CLIENTE + IMPORTE MÍNIMO
        // ==========================================================

        if (totalMinimo != null || hasParameterValue(nombreCliente)) {

            if (totalMinimo != null) {

                model.addAttribute("busquedaAvanzadaRealizada", true);

                try {

                    List<Pedido> pedidosAvanzados =
                            reporteService.buscarPedidosPorClienteYTotalMinimo(
                                    nombreCliente,
                                    totalMinimo
                            );

                    model.addAttribute(
                            "pedidosAvanzados",
                            pedidosAvanzados
                    );

                } catch (IllegalArgumentException exception) {

                    model.addAttribute(
                            "errorBusqueda",
                            exception.getMessage()
                    );
                }
            }
        }

        // ==========================================================
        // CLIENTES FRECUENTES
        // ==========================================================

        if (minPedidos != null) {

            model.addAttribute("frecuentesRealizado", true);

            try {

                List<ClienteFrecuente> clientes =
                        reporteService.listarClientesFrecuentes(minPedidos);

                model.addAttribute(
                        "clientesFrecuentes",
                        clientes
                );

            } catch (IllegalArgumentException exception) {

                model.addAttribute(
                        "errorFrecuentes",
                        exception.getMessage()
                );
            }
        }

        // ==========================================================
        // CONSULTA DE PEDIDOS POR ESTADO DE REVISIÓN
        // ==========================================================

        if (hasParameterValue(estadoRevision)) {

            String estadoNormalizado = estadoRevision.trim().toUpperCase();

            model.addAttribute("busquedaRevisionRealizada", true);

            try {

                if (!estadoNormalizado.equals("PENDIENTE")
                        && !estadoNormalizado.equals("REVISADO")) {

                    throw new IllegalArgumentException(
                            "El estado de revisión debe ser PENDIENTE o REVISADO."
                    );
                }

                model.addAttribute(
                        "estadoRevision",
                        estadoNormalizado
                );

                model.addAttribute(
                        "revisiones",
                        pedidoReporteClient.listarPorEstadoRevision(
                                estadoNormalizado
                        )
                );

            } catch (IllegalArgumentException exception) {

                model.addAttribute(
                        "errorRevision",
                        exception.getMessage()
                );

            } catch (PedidoReporteNoDisponibleException exception) {

                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

                model.addAttribute(
                        "errorRevision",
                        exception.getMessage()
                );
            }
        }

        // ==========================================================
        //  ESTADÍSTICAS DE REVISIONES
        // ==========================================================

        try {

            model.addAttribute(
                    "estadisticasRevisiones",
                    pedidoReporteClient.obtenerEstadisticasRevisiones()
            );

        } catch (PedidoReporteNoDisponibleException exception) {

            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());

            model.addAttribute(
                    "errorEstadisticasRevision",
                    exception.getMessage()
            );
        }

        return "reportes-pedidos";
    }

    private boolean hasParameterValue(String valor) {
        return valor != null && !valor.isBlank();
    }
}
