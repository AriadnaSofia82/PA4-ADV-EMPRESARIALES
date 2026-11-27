package com.isil.edu.pe.spedidos.controller.revision;

import com.isil.edu.pe.spedidos.controller.dto.PedidoRevisionRequest;
import com.isil.edu.pe.spedidos.exception.PedidoRevisionException;
import com.isil.edu.pe.spedidos.service.revision.PedidoRevisionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PedidoRevisionControllerTest {

    private PedidoRevisionService pedidoRevisionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        pedidoRevisionService = mock(PedidoRevisionService.class);

        PedidoRevisionController controller =
                new PedidoRevisionController(pedidoRevisionService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void debeMostrarPaginaDeRevision() throws Exception {

        when(pedidoRevisionService.listarPedidosPendientes())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/revision"))
                .andExpect(status().isOk())
                .andExpect(view().name("revision-pedidos"))
                .andExpect(model().attributeExists("pedidosPendientes"))
                .andExpect(model().attributeExists("pedidoRevisionRequest"));

        verify(pedidoRevisionService, times(1))
                .listarPedidosPendientes();
    }

    @Test
    void debeRegistrarRevisionCorrectamente() throws Exception {

        mockMvc.perform(post("/revision")
                        .param("pedidoId", "1")
                        .param("observacion", "Pedido revisado correctamente"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/revision"))
                .andExpect(flash().attributeExists("mensaje"));

        verify(pedidoRevisionService, times(1))
                .registrarRevision(any(PedidoRevisionRequest.class));
    }

    @Test
    void debeMostrarErrorCuandoFallaElRegistro() throws Exception {

        doThrow(new PedidoRevisionException("Error de prueba"))
                .when(pedidoRevisionService)
                .registrarRevision(any(PedidoRevisionRequest.class));

        mockMvc.perform(post("/revision")
                        .param("pedidoId", "1")
                        .param("observacion", "Observación de prueba"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/revision"))
                .andExpect(flash().attributeExists("error"));

        verify(pedidoRevisionService, times(1))
                .registrarRevision(any(PedidoRevisionRequest.class));
    }
}
