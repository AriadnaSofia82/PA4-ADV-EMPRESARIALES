package com.isil.edu.pe.spedidos.repository.mapper;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class PedidoRowMapper implements RowMapper<Pedido> {

    @Override
    public Pedido mapRow(ResultSet rs, int rowNum) throws SQLException {

        Timestamp fecha = rs.getTimestamp("fecha");

        Pedido pedido = new Pedido(
                rs.getLong("id"),
                rs.getString("cliente"),
                rs.getString("producto_codigo"),
                rs.getString("producto_nombre"),
                rs.getBigDecimal("precio_unitario"),
                rs.getInt("cantidad"),
                rs.getBigDecimal("total"),
                fecha != null ? fecha.toLocalDateTime() : null
        );

        String estado = rs.getString("estado");

        if (estado != null && !estado.isBlank()) {
            pedido.setEstado(PedidoEstado.valueOf(estado));
        }

        pedido.setRevisado(rs.getBoolean("revisado"));

        return pedido;
    }
}
