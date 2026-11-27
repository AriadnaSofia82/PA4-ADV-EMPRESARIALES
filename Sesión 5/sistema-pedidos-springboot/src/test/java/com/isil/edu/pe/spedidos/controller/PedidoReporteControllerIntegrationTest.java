package com.isil.edu.pe.spedidos.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PedidoReporteControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Limpiar primero la tabla dependiente, si existe.
        jdbcTemplate.update("DELETE FROM anulacion_pedido");
        jdbcTemplate.update("DELETE FROM pedido");

        insertarPedido(
                "Ana Torres",
                "LAP-001",
                "Laptop",
                "2500.00",
                2,
                "5000.00"
        );

        insertarPedido(
                "Carlos Perez",
                "MOU-001",
                "Mouse",
                "50.00",
                3,
                "150.00"
        );

        insertarPedido(
                "Ana Torres",
                "MOU-001",
                "Mouse",
                "50.00",
                2,
                "100.00"
        );
    }

    @Test
    @DisplayName("Devuelve el resumen general en JSON")
    void devuelveResumenGeneralEnJson() throws Exception {

        mockMvc.perform(get("/api/v1/pedidos/estadisticas/resumen")
                        .with(user("consulta").roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.totalPedidos").value(3))
                .andExpect(jsonPath("$.totalUnidades").value(7))
                .andExpect(jsonPath("$.importeAcumulado").value(5250.00))
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("Devuelve pedidos filtrados por cliente en JSON")
    void devuelvePedidosFiltradosPorClienteEnJson() throws Exception {

        mockMvc.perform(get("/api/v1/pedidos")
                        .param("cliente", "Ana")
                        .with(user("consulta").roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cliente").value("Ana Torres"))
                .andExpect(jsonPath("$._links").doesNotExist());
    }

    @Test
    @DisplayName("Devuelve productos más vendidos en formato HAL")
    void devuelveProductosMasVendidosEnFormatoHal() throws Exception {

        mockMvc.perform(get("/api/v1/pedidos/estadisticas/productos")
                        .with(user("operador").roles("OPERADOR")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaTypes.HAL_JSON
                ))
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links['estadisticas-resumen'].href").exists())
                .andExpect(jsonPath("$._links.pedidos.href").exists());
    }

    @Test
    @DisplayName("La ruta antigua de búsqueda devuelve 404")
    void rutaAntiguaDeBusquedaDevuelveNotFound() throws Exception {

        mockMvc.perform(get("/api/v1/pedidos/buscar")
                        .param("cliente", "Ana")
                        .with(user("consulta").roles("CONSULTA")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Filtra pedidos por cliente e importe mínimo")
    void filtraPedidosPorClienteEImporteMinimo() throws Exception {

        mockMvc.perform(get("/api/v1/pedidos/busqueda-avanzada")
                        .param("cliente", "Ana")
                        .param("totalMinimo", "1000")
                        .with(user("consulta").roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cliente").value("Ana Torres"))
                .andExpect(jsonPath("$[0].total").value(5000.00));
    }

    @Test
    @DisplayName("Devuelve clientes frecuentes con indicadores consolidados")
    void devuelveClientesFrecuentesConIndicadoresConsolidados() throws Exception {

        mockMvc.perform(get("/api/v1/pedidos/clientes-frecuentes")
                        .param("minPedidos", "2")
                        .with(user("consulta").roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cliente").value("Ana Torres"))
                .andExpect(jsonPath("$[0].totalPedidos").value(2))
                .andExpect(jsonPath("$[0].totalUnidades").value(4))
                .andExpect(jsonPath("$[0].importeAcumulado").value(5100.00))
                .andExpect(jsonPath("$[0].ticketPromedio").value(2550.00));
    }

    private void insertarPedido(
            String cliente,
            String productoCodigo,
            String productoNombre,
            String precioUnitario,
            int cantidad,
            String total) {

        jdbcTemplate.update(
                """
                INSERT INTO pedido (
                    cliente,
                    producto_codigo,
                    producto_nombre,
                    precio_unitario,
                    cantidad,
                    total,
                    estado,
                    revisado,
                    fecha
                ) VALUES (?, ?, ?, ?, ?, ?, 'REGISTRADO', FALSE, CURRENT_TIMESTAMP)
                """,
                cliente,
                productoCodigo,
                productoNombre,
                new BigDecimal(precioUnitario),
                cantidad,
                new BigDecimal(total)
        );
    }
}
