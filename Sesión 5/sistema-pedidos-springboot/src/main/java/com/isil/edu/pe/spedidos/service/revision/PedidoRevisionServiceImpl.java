package com.isil.edu.pe.spedidos.service.revision;

import com.isil.edu.pe.spedidos.controller.dto.PedidoRevisionRequest;
import com.isil.edu.pe.spedidos.controller.dto.PedidoRevisionResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoRevision;
import com.isil.edu.pe.spedidos.exception.PedidoRevisionException;
import com.isil.edu.pe.spedidos.repository.revision.PedidoRevisionJdbcRepository;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoRevisionServiceImpl implements PedidoRevisionService {

    private final PedidoRevisionJdbcRepository repository;

    public PedidoRevisionServiceImpl(
            PedidoRevisionJdbcRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosPendientes() {

        return repository.listarPedidosPendientes();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PedidoRevisionResponse registrarRevision(
            PedidoRevisionRequest request
    ) {

        if (request == null) {
            throw new PedidoRevisionException(
                    "La solicitud de revisión es obligatoria."
            );
        }

        if (request.getPedidoId() == null || request.getPedidoId() <= 0) {
            throw new PedidoRevisionException(
                    "El identificador del pedido no es válido."
            );
        }

        if (request.getObservacion() == null
                || request.getObservacion().isBlank()) {

            throw new PedidoRevisionException(
                    "Debe ingresar una observación."
            );
        }

        String observacion = request.getObservacion().trim();

        if (observacion.length() > 500) {
            throw new PedidoRevisionException(
                    "La observación no puede superar los 500 caracteres."
            );
        }

        try {

            int filasActualizadas =
                    repository.marcarPedidoComoRevisado(
                            request.getPedidoId()
                    );

            if (filasActualizadas != 1) {
                throw new PedidoRevisionException(
                        "El pedido no existe, ya fue revisado "
                                + "o no se encuentra en estado REGISTRADO."
                );
            }

            repository.insertarRevision(
                    request.getPedidoId(),
                    observacion
            );

            PedidoRevision revision =
                    repository.buscarRevisionPorPedidoId(
                            request.getPedidoId()
                    );

            return new PedidoRevisionResponse(
                    revision.getId(),
                    revision.getPedidoId(),
                    revision.getObservacion(),
                    revision.getFechaRevision()
            );

        } catch (PedidoRevisionException e) {

            throw e;

        } catch (DataAccessException e) {

            throw new PedidoRevisionException(
                    "No fue posible registrar la revisión del pedido. "
                            + "La operación ha sido revertida.",
                    e
            );
        }
    }
}
