
package com.isil.edu.pe.pedidos.reportes.service;

import com.isil.edu.pe.pedidos.reportes.exception.PedidoNoEncontradoException;
import com.isil.edu.pe.pedidos.reportes.repository.PedidoReporteJdbcRepository;
import com.isil.edu.pe.pedidos.reportes.repository.projection.*;
import com.isil.edu.pe.pedidos.reportes.service.domain.*;
import com.isil.edu.pe.pedidos.reportes.service.impl.PedidoReporteServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoReporteServiceImplTest {

    @Mock
    private PedidoReporteJdbcRepository jdbcRepository;

    @InjectMocks
    private PedidoReporteServiceImpl service;

    private Pedido pedido;

    @BeforeEach
    void prepararPedido() {
        pedido = new Pedido(
                1L,
                "Cliente Prueba",
                "LAP-001",
                "Laptop",
                new BigDecimal("2500.00"),
                2,
                new BigDecimal("5000.00"),
                LocalDateTime.of(2026, 11, 20, 10, 0),
                "CONFIRMADO",
                true
        );
    }

    @Test
    void obtenerPorId_pedidoExistente_devuelvePedido() {
        when(jdbcRepository.buscarPorId(1L))
                .thenReturn(Optional.of(pedido));

        Pedido resultado = service.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Cliente Prueba", resultado.cliente());

        verify(jdbcRepository).buscarPorId(1L);
    }

    @Test
    void obtenerPorId_pedidoInexistente_lanzaExcepcion() {
        when(jdbcRepository.buscarPorId(999L))
                .thenReturn(Optional.empty());

        PedidoNoEncontradoException exception =
                assertThrows(
                        PedidoNoEncontradoException.class,
                        () -> service.obtenerPorId(999L)
                );

        assertEquals(
                "No existe el pedido con id 999",
                exception.getMessage()
        );

        verify(jdbcRepository).buscarPorId(999L);
    }

    @Test
    void listarTodos_devuelvePedidosDelRepositorio() {
        when(jdbcRepository.listarTodos())
                .thenReturn(List.of(pedido));

        List<Pedido> resultado = service.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).id());

        verify(jdbcRepository).listarTodos();
    }

    @Test
    void obtenerResumen_convierteProyeccionADominio() {
        when(jdbcRepository.obtenerResumen())
                .thenReturn(new ResumenPedidosProjection(
                        10,
                        25,
                        new BigDecimal("15000.00"),
                        new BigDecimal("1500.00")
                ));

        ResumenPedidos resultado = service.obtenerResumen();

        assertEquals(10L, resultado.totalPedidos());
        assertEquals(25L, resultado.totalUnidades());
        assertEquals(
                new BigDecimal("15000.00"),
                resultado.importeAcumulado()
        );
        assertEquals(
                new BigDecimal("1500.00"),
                resultado.ticketPromedio()
        );

        verify(jdbcRepository).obtenerResumen();
    }

    @Test
    void buscarPorCliente_clienteValido_consultaRepositorio() {
        when(jdbcRepository.buscarPorCliente("Cliente A"))
                .thenReturn(List.of(pedido));

        List<Pedido> resultado =
                service.buscarPorCliente("Cliente A");

        assertEquals(1, resultado.size());

        verify(jdbcRepository).buscarPorCliente("Cliente A");
    }

    @Test
    void buscarPorCliente_clienteNulo_devuelveListaVacia() {
        List<Pedido> resultado = service.buscarPorCliente(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verifyNoInteractions(jdbcRepository);
    }

    @Test
    void buscarPorCliente_clienteEnBlanco_devuelveListaVacia() {
        List<Pedido> resultado = service.buscarPorCliente("   ");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verifyNoInteractions(jdbcRepository);
    }

    @Test
    void listarProductosMasVendidos_convierteProyecciones() {
        when(jdbcRepository.listarProductosMasVendidos())
                .thenReturn(List.of(
                        new ProductoVendidoProjection(
                                "LAP-001",
                                "Laptop",
                                5,
                                10,
                                new BigDecimal("25000.00")
                        )
                ));

        List<ProductoVendido> resultado =
                service.listarProductosMasVendidos();

        assertEquals(1, resultado.size());

        ProductoVendido producto = resultado.get(0);

        assertEquals("LAP-001", producto.productoCodigo());
        assertEquals("Laptop", producto.productoNombre());
        assertEquals(5L, producto.totalPedidos());
        assertEquals(10L, producto.totalUnidades());
        assertEquals(
                new BigDecimal("25000.00"),
                producto.importeAcumulado()
        );

        verify(jdbcRepository).listarProductosMasVendidos();
    }

    @Test
    void listarPorEstadoRevision_estadoPendiente_filtraCorrectamente() {
        Pedido pedidoPendiente = new Pedido(
                2L,
                "Cliente B",
                "MON-001",
                "Monitor",
                new BigDecimal("850.00"),
                1,
                new BigDecimal("850.00"),
                LocalDateTime.now(),
                "CONFIRMADO",
                false
        );

        when(jdbcRepository.listarPorEstadoRevision(false))
                .thenReturn(List.of(
                        new PedidoRevisionProjection(
                                pedidoPendiente,
                                null,
                                null
                        )
                ));

        List<PedidoRevision> resultado =
                service.listarPorEstadoRevision(EstadoRevision.PENDIENTE);

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).pedido().id());
        assertFalse(resultado.get(0).pedido().revisado());
        assertNull(resultado.get(0).observacion());

        verify(jdbcRepository).listarPorEstadoRevision(false);
    }

    @Test
    void listarPorEstadoRevision_estadoRevisado_devuelveTrazabilidad() {
        LocalDateTime fechaRevision =
                LocalDateTime.of(2026, 11, 21, 12, 0);

        when(jdbcRepository.listarPorEstadoRevision(true))
                .thenReturn(List.of(
                        new PedidoRevisionProjection(
                                pedido,
                                "Pedido verificado",
                                fechaRevision
                        )
                ));

        List<PedidoRevision> resultado =
                service.listarPorEstadoRevision(EstadoRevision.REVISADO);

        assertEquals(1, resultado.size());
        assertEquals("Pedido verificado", resultado.get(0).observacion());
        assertEquals(fechaRevision, resultado.get(0).fechaRevision());

        verify(jdbcRepository).listarPorEstadoRevision(true);
    }

    @Test
    void listarPorEstadoRevision_estadoNulo_lanzaExcepcion() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.listarPorEstadoRevision(null)
                );

        assertEquals(
                "Debe especificar un estado de revisión válido.",
                exception.getMessage()
        );

        verifyNoInteractions(jdbcRepository);
    }

    @Test
    void obtenerEstadisticasRevisiones_convierteProyeccion() {
        when(jdbcRepository.obtenerEstadisticasRevisiones())
                .thenReturn(new EstadisticasRevisionesProjection(
                        20,
                        15,
                        5,
                        new BigDecimal("75.00")
                ));

        EstadisticasRevisiones resultado =
                service.obtenerEstadisticasRevisiones();

        assertEquals(20L, resultado.totalPedidos());
        assertEquals(15L, resultado.totalRevisados());
        assertEquals(5L, resultado.totalPendientes());
        assertEquals(
                new BigDecimal("75.00"),
                resultado.porcentajeRevisado()
        );

        verify(jdbcRepository).obtenerEstadisticasRevisiones();
    }
}
