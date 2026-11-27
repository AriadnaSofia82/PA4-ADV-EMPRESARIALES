package com.isil.edu.pe.spedidos.service.revision;

import com.isil.edu.pe.spedidos.controller.dto.PedidoRevisionRequest;
import com.isil.edu.pe.spedidos.controller.dto.PedidoRevisionResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;

import java.util.List;

public interface PedidoRevisionService {

    List<Pedido> listarPedidosPendientes();

    PedidoRevisionResponse registrarRevision(
            PedidoRevisionRequest request
    );
}
