
package com.isil.edu.pe.pedidos.reportes.controller;

import com.isil.edu.pe.pedidos.reportes.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.pedidos.reportes.service.PedidoReporteService;
import com.isil.edu.pe.pedidos.reportes.service.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoReporteController.class)
class PedidoReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoReporteService reporteService;

    private Pedido crearPedido(boolean revisado) {
        return new Pedido(
                1L,
                "Cliente Prueba",
                "LAP-001",
                "Laptop",
                new BigDecimal("2500.00"),
                2,
                new BigDecimal("5000.00"),
                LocalDateTime.of(2026, 11, 20, 10, 30),
                "CONFIRMADO",
                revisado
        );
    }

    @Test
    void obtenerPorId_devuelvePedidoCorrectamente() throws Exception {
        when(reporteService.obtenerPorId(1L))
                .thenReturn(crearPedido(false));

        mockMvc.perform(get("/api/v1/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cliente").value("Cliente Prueba"))
                .andExpect(jsonPath("$.productoCodigo").value("LAP-001"))
                .andExpect(jsonPath("$.productoNombre").value("Laptop"))
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.total").value(5000.00))
                .andExpect(jsonPath("$.revisado").value(false));

        verify(reporteService).obtenerPorId(1L);
    }

    @Test
    void obtenerPorId_pedidoNoEncontrado_devuelve404() throws Exception {
        when(reporteService.obtenerPorId(999L))
                .thenThrow(new PedidoNoEncontradoException(999L));

        mockMvc.perform(get("/api/v1/pedidos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Pedido no encontrado"))
                .andExpect(jsonPath("$.detail")
                        .value("No existe el pedido con id 999"));
    }

    @Test
    void listarPedidosPorCliente_devuelveResultados() throws Exception {
        when(reporteService.buscarPorCliente("Cliente Prueba"))
                .thenReturn(List.of(crearPedido(false)));

        mockMvc.perform(get("/api/v1/pedidos")
                        .param("cliente", "Cliente Prueba"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].cliente").value("Cliente Prueba"))
                .andExpect(jsonPath("$[0].productoCodigo").value("LAP-001"));

        verify(reporteService).buscarPorCliente("Cliente Prueba");
    }

    @Test
    void obtenerResumen_devuelveIndicadores() throws Exception {
        when(reporteService.obtenerResumen())
                .thenReturn(new ResumenPedidos(
                        10,
                        25,
                        new BigDecimal("15000.00"),
                        new BigDecimal("1500.00")
                ));

        mockMvc.perform(get("/api/v1/pedidos/estadisticas/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPedidos").value(10))
                .andExpect(jsonPath("$.totalUnidades").value(25))
                .andExpect(jsonPath("$.importeAcumulado").value(15000.00))
                .andExpect(jsonPath("$.ticketPromedio").value(1500.00));

        verify(reporteService).obtenerResumen();
    }

    @Test
    void listarProductosMasVendidos_devuelveProductos() throws Exception {
        when(reporteService.listarProductosMasVendidos())
                .thenReturn(List.of(
                        new ProductoVendido(
                                "LAP-001",
                                "Laptop",
                                5,
                                10,
                                new BigDecimal("25000.00")
                        )
                ));

        mockMvc.perform(get("/api/v1/pedidos/estadisticas/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productoCodigo").value("LAP-001"))
                .andExpect(jsonPath("$[0].productoNombre").value("Laptop"))
                .andExpect(jsonPath("$[0].totalPedidos").value(5))
                .andExpect(jsonPath("$[0].totalUnidades").value(10))
                .andExpect(jsonPath("$[0].importeAcumulado").value(25000.00));

        verify(reporteService).listarProductosMasVendidos();
    }

    @Test
    void listarPorEstadoRevision_devuelvePedidosPendientes() throws Exception {
        Pedido pedido = crearPedido(false);

        when(reporteService.listarPorEstadoRevision(EstadoRevision.PENDIENTE))
                .thenReturn(List.of(
                        new PedidoRevision(pedido, null, null)
                ));

        mockMvc.perform(get("/api/v1/pedidos/revisiones")
                        .param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].revisado").value(false))
                .andExpect(jsonPath("$[0].observacion").value(nullValue()));

        verify(reporteService)
                .listarPorEstadoRevision(EstadoRevision.PENDIENTE);
    }

    @Test
    void listarPorEstadoRevision_devuelvePedidosRevisados() throws Exception {
        Pedido pedido = crearPedido(true);

        LocalDateTime fechaRevision =
                LocalDateTime.of(2026, 11, 21, 11, 0);

        when(reporteService.listarPorEstadoRevision(EstadoRevision.REVISADO))
                .thenReturn(List.of(
                        new PedidoRevision(
                                pedido,
                                "Pedido verificado correctamente",
                                fechaRevision
                        )
                ));

        mockMvc.perform(get("/api/v1/pedidos/revisiones")
                        .param("estado", "REVISADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].revisado").value(true))
                .andExpect(jsonPath("$[0].observacion")
                        .value("Pedido verificado correctamente"))
                .andExpect(jsonPath("$[0].fechaRevision").exists());

        verify(reporteService)
                .listarPorEstadoRevision(EstadoRevision.REVISADO);
    }

    @Test
    void listarPorEstadoRevision_estadoInvalido_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos/revisiones")
                        .param("estado", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Parámetro inválido"));

        verifyNoInteractions(reporteService);
    }

    @Test
    void obtenerEstadisticasRevisiones_devuelveIndicadores() throws Exception {
        when(reporteService.obtenerEstadisticasRevisiones())
                .thenReturn(new EstadisticasRevisiones(
                        20,
                        15,
                        5,
                        new BigDecimal("75.00")
                ));

        mockMvc.perform(get("/api/v1/pedidos/estadisticas/revisiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPedidos").value(20))
                .andExpect(jsonPath("$.totalRevisados").value(15))
                .andExpect(jsonPath("$.totalPendientes").value(5))
                .andExpect(jsonPath("$.porcentajeRevisado").value(75.00));

        verify(reporteService).obtenerEstadisticasRevisiones();
    }
}
