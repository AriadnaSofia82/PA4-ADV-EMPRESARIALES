package com.isil.edu.pe.spedidos.service.revision;
import com.isil.edu.pe.spedidos.controller.dto.PedidoRevisionRequest;
import com.isil.edu.pe.spedidos.controller.dto.PedidoRevisionResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoRevision;
import com.isil.edu.pe.spedidos.exception.PedidoRevisionException;
import com.isil.edu.pe.spedidos.repository.revision.PedidoRevisionJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoRevisionServiceImplTest {

    private PedidoRevisionJdbcRepository repository;

    private PedidoRevisionServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(PedidoRevisionJdbcRepository.class);
        service = new PedidoRevisionServiceImpl(repository);
    }

    @Test
    void debeListarPedidosPendientes() {

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCliente("Juan Perez");

        when(repository.listarPedidosPendientes())
                .thenReturn(List.of(pedido));

        List<Pedido> resultado = service.listarPedidosPendientes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.getFirst().getId());
        assertEquals("Juan Perez", resultado.getFirst().getCliente());

        verify(repository, times(1))
                .listarPedidosPendientes();
    }

    @Test
    void debeRegistrarRevisionCorrectamente() {

        PedidoRevisionRequest request = new PedidoRevisionRequest();
        request.setPedidoId(1L);
        request.setObservacion("Pedido revisado correctamente");

        LocalDateTime fecha = LocalDateTime.of(
                2026, 11, 25, 10, 30
        );

        PedidoRevision revision = new PedidoRevision(
                1L,
                1L,
                "Pedido revisado correctamente",
                fecha
        );

        when(repository.marcarPedidoComoRevisado(1L))
                .thenReturn(1);

        when(repository.buscarRevisionPorPedidoId(1L))
                .thenReturn(revision);

        PedidoRevisionResponse resultado =
                service.registrarRevision(request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(1L, resultado.getPedidoId());
        assertEquals(
                "Pedido revisado correctamente",
                resultado.getObservacion()
        );
        assertEquals(fecha, resultado.getFechaRevision());

        verify(repository, times(1))
                .marcarPedidoComoRevisado(1L);

        verify(repository, times(1))
                .insertarRevision(
                        1L,
                        "Pedido revisado correctamente"
                );

        verify(repository, times(1))
                .buscarRevisionPorPedidoId(1L);
    }

    @Test
    void debeRechazarRevisionCuandoElPedidoNoExisteOYaFueRevisado() {

        PedidoRevisionRequest request = new PedidoRevisionRequest();
        request.setPedidoId(99L);
        request.setObservacion("Revisión de prueba");

        when(repository.marcarPedidoComoRevisado(99L))
                .thenReturn(0);

        assertThrows(
                PedidoRevisionException.class,
                () -> service.registrarRevision(request)
        );

        verify(repository, times(1))
                .marcarPedidoComoRevisado(99L);

        verify(repository, never())
                .insertarRevision(anyLong(), anyString());

        verify(repository, never())
                .buscarRevisionPorPedidoId(anyLong());
    }

    @Test
    void debeRechazarSolicitudNula() {

        assertThrows(
                PedidoRevisionException.class,
                () -> service.registrarRevision(null)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void debeRechazarIdentificadorDePedidoInvalido() {

        PedidoRevisionRequest request = new PedidoRevisionRequest();
        request.setPedidoId(0L);
        request.setObservacion("Observación de prueba");

        assertThrows(
                PedidoRevisionException.class,
                () -> service.registrarRevision(request)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void debeRechazarObservacionVacia() {

        PedidoRevisionRequest request = new PedidoRevisionRequest();
        request.setPedidoId(1L);
        request.setObservacion("   ");

        assertThrows(
                PedidoRevisionException.class,
                () -> service.registrarRevision(request)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void debeRechazarObservacionMayorA500Caracteres() {

        // Arrange
        PedidoRevisionRequest request = new PedidoRevisionRequest();
        request.setPedidoId(1L);
        request.setObservacion("A".repeat(501));

        // Act + Assert
        assertThrows(
                PedidoRevisionException.class,
                () -> service.registrarRevision(request)
        );

        verifyNoInteractions(repository);
    }
}
