
package com.isil.edu.pe.pedidos.reportes.repository.mapper;

import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class PedidoRowMapper implements RowMapper<Pedido> {

  @Override
  public Pedido mapRow(ResultSet rs, int rowNum)
          throws SQLException {

    return new Pedido(
            rs.getLong("id"),
            rs.getString("cliente"),
            rs.getString("producto_codigo"),
            rs.getString("producto_nombre"),
            rs.getBigDecimal("precio_unitario"),
            rs.getInt("cantidad"),
            rs.getBigDecimal("total"),
            rs.getTimestamp("fecha").toLocalDateTime(),
            rs.getString("estado"),
            rs.getBoolean("revisado")
    );
  }
}
