package com.isil.edu.pe.spedidos.repository.revision;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoRevision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PedidoRevisionJdbcRepositoryTest {

    private JdbcTemplate jdbcTemplate;

    private PedidoRevisionJdbcRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        repository = new PedidoRevisionJdbcRepository(jdbcTemplate);
    }

    @Test
    void debeListarPedidosPendientes() {

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCliente("Juan Perez");
        pedido.setProductoCodigo("LAP-001");
        pedido.setProductoNombre("Laptop");
        pedido.setPrecioUnitario(new BigDecimal("2500.00"));
        pedido.setCantidad(1);
        pedido.setTotal(new BigDecimal("2500.00"));

        when(jdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<Pedido>>any()
        )).thenReturn(List.of(pedido));

        List<Pedido> resultado = repository.listarPedidosPendientes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.getFirst().getId());
        assertEquals("Juan Perez", resultado.getFirst().getCliente());

        verify(jdbcTemplate).query(
                contains("FROM pedido"),
                org.mockito.ArgumentMatchers.<RowMapper<Pedido>>any()
        );
    }

    @Test
    void debeMarcarPedidoComoRevisado() {

        when(jdbcTemplate.update(
                anyString(),
                eq(1L)
        )).thenReturn(1);

        int resultado = repository.marcarPedidoComoRevisado(1L);

        assertEquals(1, resultado);

        verify(jdbcTemplate).update(
                contains("UPDATE pedido"),
                eq(1L)
        );
    }

    @Test
    void noDebeMarcarPedidoCuandoNoExisteOYaFueRevisado() {

        when(jdbcTemplate.update(
                anyString(),
                eq(99L)
        )).thenReturn(0);

        int resultado = repository.marcarPedidoComoRevisado(99L);

        assertEquals(0, resultado);

        verify(jdbcTemplate).update(
                contains("revisado = FALSE"),
                eq(99L)
        );
    }

    @Test
    void debeInsertarRevision() {

        when(jdbcTemplate.update(
                anyString(),
                eq(1L),
                eq("Pedido revisado correctamente")
        )).thenReturn(1);

        assertDoesNotThrow(() ->
                repository.insertarRevision(
                        1L,
                        "Pedido revisado correctamente"
                )
        );

        verify(jdbcTemplate).update(
                contains("INSERT INTO pedido_revision"),
                eq(1L),
                eq("Pedido revisado correctamente")
        );
    }

    @Test
    void debeBuscarRevisionPorPedidoId() {

        LocalDateTime fecha = LocalDateTime.of(
                2026, 11, 25, 10, 30
        );

        PedidoRevision revision = new PedidoRevision(
                1L,
                1L,
                "Pedido revisado correctamente",
                fecha
        );

        when(jdbcTemplate.queryForObject(
                anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<PedidoRevision>>any(),
                eq(1L)
        )).thenReturn(revision);

        PedidoRevision resultado =
                repository.buscarRevisionPorPedidoId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getPedidoId());
        assertEquals(
                "Pedido revisado correctamente",
                resultado.getObservacion()
        );
        assertEquals(fecha, resultado.getFechaRevision());

        verify(jdbcTemplate).queryForObject(
                contains("FROM pedido_revision"),
                org.mockito.ArgumentMatchers.<RowMapper<PedidoRevision>>any(),
                eq(1L)
        );
    }
}
