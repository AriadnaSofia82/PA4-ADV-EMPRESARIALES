package com.isil.edu.pe.spedidos.repository.revision;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class PedidoRevisionJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public PedidoRevisionJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Pedido> pedidoRowMapper = (rs, rowNum) -> {
        Pedido pedido = new Pedido();

        pedido.setId(rs.getLong("id"));
        pedido.setCliente(rs.getString("cliente"));
        pedido.setProductoCodigo(rs.getString("producto_codigo"));
        pedido.setProductoNombre(rs.getString("producto_nombre"));
        pedido.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        pedido.setCantidad(rs.getInt("cantidad"));
        pedido.setTotal(rs.getBigDecimal("total"));

        Timestamp fecha = rs.getTimestamp("fecha");

        if (fecha != null) {
            pedido.setFecha(fecha.toLocalDateTime());
        }

        pedido.setEstado(
                PedidoEstado.valueOf(rs.getString("estado"))
        );

        pedido.setRevisado(rs.getBoolean("revisado"));

        return pedido;
    };

    /**
     * Obtiene los pedidos registrados que todavía no han sido revisados.
     */
    public List<Pedido> listarPedidosPendientes() {

        String sql = """
                SELECT id,
                       cliente,
                       producto_codigo,
                       producto_nombre,
                       precio_unitario,
                       cantidad,
                       total,
                       fecha,
                       estado,
                       revisado
                FROM pedido
                WHERE revisado = FALSE
                  AND estado = 'REGISTRADO'
                ORDER BY fecha DESC, id DESC
                """;

        return jdbcTemplate.query(sql, pedidoRowMapper);
    }

    /**
     * Marca un pedido como revisado.
     *
     * Solo permite actualizar pedidos registrados
     * que todavía no hayan sido revisados.
     */
    public int marcarPedidoComoRevisado(Long pedidoId) {

        String sql = """
                UPDATE pedido
                SET revisado = TRUE
                WHERE id = ?
                  AND revisado = FALSE
                  AND estado = 'REGISTRADO'
                """;

        return jdbcTemplate.update(sql, pedidoId);
    }

    /**
     * Inserta el registro de revisión.
     *
     * La fecha se genera automáticamente desde MySQL.
     */
    public void insertarRevision(Long pedidoId, String observacion) {

        String sql = """
                INSERT INTO pedido_revision (
                    pedido_id,
                    observacion
                )
                VALUES (?, ?)
                """;

        jdbcTemplate.update(
                sql,
                pedidoId,
                observacion
        );
    }

    /**
     * Obtiene una revisión por su identificador de pedido.
     */
    public com.isil.edu.pe.spedidos.entity.PedidoRevision
    buscarRevisionPorPedidoId(Long pedidoId) {

        String sql = """
                SELECT id,
                       pedido_id,
                       observacion,
                       fecha_revision
                FROM pedido_revision
                WHERE pedido_id = ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new com.isil.edu.pe.spedidos.entity.PedidoRevision(
                        rs.getLong("id"),
                        rs.getLong("pedido_id"),
                        rs.getString("observacion"),
                        rs.getTimestamp("fecha_revision").toLocalDateTime()
                ),
                pedidoId
        );
    }
}
