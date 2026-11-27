package com.isil.edu.pe.pedidos.reportes.repository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.isil.edu.pe.pedidos.reportes.repository.mapper.PedidoRowMapper;
import com.isil.edu.pe.pedidos.reportes.repository.projection.EstadisticasRevisionesProjection;
import com.isil.edu.pe.pedidos.reportes.repository.projection.PedidoRevisionProjection;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ProductoVendidoProjection;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ResumenPedidosProjection;
import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
/**
 * Repositorio de consultas y reportes implementado
 * mediante Spring JDBC.
 */
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

    public Optional<Pedido> buscarPorId(long id) {
        String sql = """
                SELECT %s
                FROM pedido
                WHERE id = ?
                """.formatted(COLUMNAS_PEDIDO);

        return jdbcTemplate.query(sql, pedidoRowMapper, id)
                .stream()
                .findFirst();
    }

    public List<Pedido> listarTodos() {
        String sql = """
                SELECT %s
                FROM pedido
                ORDER BY id DESC
                """.formatted(COLUMNAS_PEDIDO);

        return jdbcTemplate.query(sql, pedidoRowMapper);
    }

    /**
     * Obtiene indicadores consolidados de todos los pedidos.
     */
    public ResumenPedidosProjection obtenerResumen() {
        String sql = """
                SELECT
                    COUNT(*) AS total_pedidos,
                    COALESCE(SUM(cantidad), 0) AS total_unidades,
                    COALESCE(SUM(total), 0) AS importe_acumulado,
                    COALESCE(AVG(total), 0) AS ticket_promedio
                FROM pedido
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new ResumenPedidosProjection(
                        rs.getLong("total_pedidos"),
                        rs.getLong("total_unidades"),
                        valorMonetario(rs.getBigDecimal("importe_acumulado")),
                        valorMonetario(rs.getBigDecimal("ticket_promedio"))
                )
        );
    }

    /**
     * Busca pedidos por nombre o parte del nombre del cliente.
     */
    public List<Pedido> buscarPorCliente(String cliente) {
        String sql = """
                SELECT %s
                FROM pedido
                WHERE LOWER(cliente) LIKE LOWER(:cliente)
                ORDER BY id DESC
                """.formatted(COLUMNAS_PEDIDO);

        Map<String, Object> parametros = Map.of(
                "cliente",
                "%" + cliente.trim() + "%"
        );

        return namedParameterJdbcTemplate.query(
                sql,
                parametros,
                pedidoRowMapper
        );
    }

    /**
     * Consolida los pedidos por producto.
     */
    public List<ProductoVendidoProjection> listarProductosMasVendidos() {
        String sql = """
                SELECT
                    producto_codigo,
                    producto_nombre,
                    COUNT(*) AS total_pedidos,
                    SUM(cantidad) AS total_unidades,
                    SUM(total) AS importe_acumulado
                FROM pedido
                GROUP BY
                    producto_codigo,
                    producto_nombre
                ORDER BY
                    total_unidades DESC,
                    importe_acumulado DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new ProductoVendidoProjection(
                        rs.getString("producto_codigo"),
                        rs.getString("producto_nombre"),
                        rs.getLong("total_pedidos"),
                        rs.getLong("total_unidades"),
                        valorMonetario(rs.getBigDecimal("importe_acumulado"))
                )
        );
    }

    /**
     * Lista los pedidos según su estado de revisión.
     *
     * false = PENDIENTE
     * true  = REVISADO
     */
    /**
     * Lista pedidos y sus datos de trazabilidad según el estado de revisión.
     *
     * LEFT JOIN permite incluir pedidos pendientes,
     * aunque todavía no tengan un registro en pedido_revision.
     */
    public List<PedidoRevisionProjection> listarPorEstadoRevision(
            boolean revisado) {

        String sql = """
            SELECT
                p.id,
                p.cliente,
                p.producto_codigo,
                p.producto_nombre,
                p.precio_unitario,
                p.cantidad,
                p.total,
                p.fecha,
                p.estado,
                p.revisado,
                pr.observacion,
                pr.fecha_revision
            FROM pedido p
            LEFT JOIN pedido_revision pr
                ON p.id = pr.pedido_id
            WHERE p.revisado = ?
            ORDER BY p.id DESC
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Pedido pedido = pedidoRowMapper.mapRow(rs, rowNum);

                    Timestamp fechaRevision =
                            rs.getTimestamp("fecha_revision");

                    return new PedidoRevisionProjection(
                            pedido,
                            rs.getString("observacion"),
                            fechaRevision != null
                                    ? fechaRevision.toLocalDateTime()
                                    : null
                    );
                },
                revisado
        );
    }

    /**
     * Obtiene las estadísticas generales de revisión.
     */
    public EstadisticasRevisionesProjection obtenerEstadisticasRevisiones() {
        String sql = """
                SELECT
                    COUNT(*) AS total_pedidos,

                    COALESCE(
                        SUM(CASE
                            WHEN revisado = 1 THEN 1
                            ELSE 0
                        END),
                        0
                    ) AS total_revisados,

                    COALESCE(
                        SUM(CASE
                            WHEN revisado = 0 THEN 1
                            ELSE 0
                        END),
                        0
                    ) AS total_pendientes,

                    COALESCE(
                        ROUND(
                            (
                                SUM(CASE
                                    WHEN revisado = 1 THEN 1
                                    ELSE 0
                                END) * 100.0
                            ) / NULLIF(COUNT(*), 0),
                            2
                        ),
                        0
                    ) AS porcentaje_revisado

                FROM pedido
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new EstadisticasRevisionesProjection(
                        rs.getLong("total_pedidos"),
                        rs.getLong("total_revisados"),
                        rs.getLong("total_pendientes"),
                        valorMonetario(rs.getBigDecimal("porcentaje_revisado"))
                )
        );
    }

    private BigDecimal valorMonetario(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}