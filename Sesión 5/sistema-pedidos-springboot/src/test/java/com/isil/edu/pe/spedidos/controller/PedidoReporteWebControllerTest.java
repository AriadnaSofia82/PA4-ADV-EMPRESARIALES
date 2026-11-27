package com.isil.edu.pe.spedidos.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.dto.ProductoVendidoResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ResumenPedidosResponse;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import com.isil.edu.pe.spedidos.service.domain.ClienteFrecuente;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class PedidoReporteWebControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PedidoReporteClient pedidoReporteClient;

    @MockitoBean
    private PedidoReporteService reporteService;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        when(pedidoReporteClient.obtenerResumen())
                .thenReturn(new ResumenPedidosResponse(
                        0L,
                        0L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));

        when(pedidoReporteClient.listarProductosMasVendidos())
                .thenReturn(List.<ProductoVendidoResponse>of());
    }

    @Test
    @DisplayName("Renderiza reportes para un usuario CONSULTA")
    void renderizaReportesParaUsuarioConsulta() throws Exception {

        when(pedidoReporteClient.buscarPorCliente("Ana"))
                .thenReturn(List.of());

        mockMvc.perform(get("/reportes/pedidos")
                        .param("cliente", "Ana")
                        .with(user("consulta").roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(view().name("reportes-pedidos"))
                .andExpect(model().attributeExists(
                        "resumen",
                        "productos",
                        "pedidos",
                        "cliente",
                        "pedidosAvanzados",
                        "clientesFrecuentes"
                ))
                .andExpect(model().attribute(
                        "busquedaSimpleRealizada",
                        true
                ));
    }

    @Test
    @DisplayName("Ejecuta la búsqueda avanzada local")
    void ejecutaBusquedaAvanzadaLocal() throws Exception {

        when(reporteService.buscarPedidosPorClienteYTotalMinimo(
                "Ana",
                new BigDecimal("1000")
        )).thenReturn(List.of());

        mockMvc.perform(get("/reportes/pedidos")
                        .param("cliente", "Ana")
                        .param("totalMinimo", "1000")
                        .with(user("consulta").roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(view().name("reportes-pedidos"))
                .andExpect(model().attribute(
                        "busquedaAvanzadaRealizada",
                        true
                ));
    }

    @Test
    @DisplayName("Ejecuta la consulta de clientes frecuentes")
    void ejecutaConsultaDeClientesFrecuentes() throws Exception {

        when(reporteService.listarClientesFrecuentes(2))
                .thenReturn(List.<ClienteFrecuente>of());

        mockMvc.perform(get("/reportes/pedidos")
                        .param("minPedidos", "2")
                        .with(user("consulta").roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(view().name("reportes-pedidos"))
                .andExpect(model().attribute(
                        "frecuentesRealizado",
                        true
                ))
                .andExpect(model().attributeExists("clientesFrecuentes"));
    }
}
