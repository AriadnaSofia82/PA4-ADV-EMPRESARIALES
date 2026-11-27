package com.isil.edu.pe.spedidos.repository;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.mapper.PedidoRowMapper;
import com.isil.edu.pe.spedidos.repository.projection.ClienteFrecuenteProjection;
import com.isil.edu.pe.spedidos.repository.projection.ProductoVendidoProjection;
import com.isil.edu.pe.spedidos.repository.projection.ResumenPedidosProjection;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoReporteJdbcRepository {

    private static final String COLUMNAS_PEDIDO = """
            id,
            cliente,
            producto_codigo,
            producto_nombre,
            precio_unitario,
            cantidad,
            total,
            fecha,
            estado,
            revisado
            """;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PedidoRowMapper pedidoRowMapper;

    public PedidoReporteJdbcRepository(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            PedidoRowMapper pedidoRowMapper) {

        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.pedidoRowMapper = pedidoRowMapper;
    }

    public ResumenPedidosProjection obtenerResumen() {

        String sql = """
                SELECT
                    COUNT(*) AS total_pedidos,
                    COALESCE(SUM(cantidad), 0) AS total_unidades,
                    COALESCE(SUM(total), 0) AS importe_acumulado,
                    COALESCE(AVG(total), 0) AS ticket_promedio
                FROM pedido
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new ResumenPedidosProjection(
                        rs.getLong("total_pedidos"),
                        rs.getLong("total_unidades"),
                        valorMonetario(rs.getBigDecimal("importe_acumulado")),
                        valorMonetario(rs.getBigDecimal("ticket_promedio"))
                )
        );
    }

    public List<Pedido> buscarPorCliente(String cliente) {

        String sql = """
                SELECT %s
                FROM pedido
                WHERE LOWER(cliente) LIKE LOWER(:cliente)
                ORDER BY id DESC
                """.formatted(COLUMNAS_PEDIDO);

        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("cliente", "%" + cliente.trim() + "%");

        return namedParameterJdbcTemplate.query(
                sql,
                parametros,
                pedidoRowMapper
        );
    }

    public List<Pedido> buscarPedidosPorClienteYTotalMinimo(
            String cliente,
            BigDecimal totalMinimo) {

        String sql = """
                SELECT %s
                FROM pedido
                WHERE LOWER(cliente) LIKE LOWER(:cliente)
                  AND total >= :totalMinimo
                ORDER BY id DESC
                """.formatted(COLUMNAS_PEDIDO);

        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("cliente", "%" + cliente.trim() + "%")
                .addValue("totalMinimo", totalMinimo);

        return namedParameterJdbcTemplate.query(
                sql,
                parametros,
                pedidoRowMapper
        );
    }

    public List<ProductoVendidoProjection> listarProductosMasVendidos() {

        String sql = """
                SELECT
                    producto_codigo,
                    producto_nombre,
                    COUNT(*) AS total_pedidos,
                    COALESCE(SUM(cantidad), 0) AS total_unidades,
                    COALESCE(SUM(total), 0) AS importe_acumulado
                FROM pedido
                GROUP BY producto_codigo, producto_nombre
                ORDER BY total_unidades DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new ProductoVendidoProjection(
                        rs.getString("producto_codigo"),
                        rs.getString("producto_nombre"),
                        rs.getLong("total_pedidos"),
                        rs.getLong("total_unidades"),
                        valorMonetario(rs.getBigDecimal("importe_acumulado"))
                )
        );
    }

    public List<ClienteFrecuenteProjection> listarClientesFrecuentes(
            int minPedidos) {

        String sql = """
                SELECT
                    cliente,
                    COUNT(*) AS total_pedidos,
                    COALESCE(SUM(cantidad), 0) AS total_unidades,
                    COALESCE(SUM(total), 0) AS importe_acumulado,
                    COALESCE(AVG(total), 0) AS ticket_promedio
                FROM pedido
                GROUP BY cliente
                HAVING COUNT(*) >= :minPedidos
                ORDER BY total_pedidos DESC
                """;

        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("minPedidos", minPedidos);

        return namedParameterJdbcTemplate.query(
                sql,
                parametros,
                (rs, rowNum) -> new ClienteFrecuenteProjection(
                        rs.getString("cliente"),
                        rs.getLong("total_pedidos"),
                        rs.getLong("total_unidades"),
                        valorMonetario(rs.getBigDecimal("importe_acumulado")),
                        valorMonetario(rs.getBigDecimal("ticket_promedio"))
                )
        );
    }

    private BigDecimal valorMonetario(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
