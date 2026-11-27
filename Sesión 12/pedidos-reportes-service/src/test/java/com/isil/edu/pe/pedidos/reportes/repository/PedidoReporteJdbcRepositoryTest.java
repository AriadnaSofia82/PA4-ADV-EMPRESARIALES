
package com.isil.edu.pe.pedidos.reportes.repository;

import com.isil.edu.pe.pedidos.reportes.repository.mapper.PedidoRowMapper;
import com.isil.edu.pe.pedidos.reportes.repository.projection.EstadisticasRevisionesProjection;
import com.isil.edu.pe.pedidos.reportes.repository.projection.PedidoRevisionProjection;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ProductoVendidoProjection;
import com.isil.edu.pe.pedidos.reportes.repository.projection.ResumenPedidosProjection;
import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:reportes_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@Import({
        PedidoReporteJdbcRepository.class,
        PedidoRowMapper.class
})
class PedidoReporteJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PedidoReporteJdbcRepository repository;

    @BeforeEach
    void prepararDatos() {

        jdbcTemplate.execute("DROP TABLE IF EXISTS pedido_revision");
        jdbcTemplate.execute("DROP TABLE IF EXISTS pedido");

        jdbcTemplate.execute("""
                CREATE TABLE pedido (
                    id BIGINT PRIMARY KEY,
                    cliente VARCHAR(150) NOT NULL,
                    producto_codigo VARCHAR(30) NOT NULL,
                    producto_nombre VARCHAR(150) NOT NULL,
                    precio_unitario DECIMAL(12,2) NOT NULL,
                    cantidad INT NOT NULL,
                    total DECIMAL(12,2) NOT NULL,
                    fecha TIMESTAMP NOT NULL,
                    estado VARCHAR(30) NOT NULL,
                    revisado TINYINT NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE pedido_revision (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    pedido_id BIGINT NOT NULL,
                    observacion VARCHAR(500),
                    fecha_revision TIMESTAMP,
                    FOREIGN KEY (pedido_id) REFERENCES pedido(id)
                )
                """);

        insertarPedido(
                1L, "Cliente A", "LAP-001", "Laptop",
                "2500.00", 2, "5000.00", false
        );

        insertarPedido(
                2L, "Cliente B", "MON-001", "Monitor",
                "850.00", 3, "2550.00", true
        );

        insertarPedido(
                3L, "Cliente A", "LAP-001", "Laptop",
                "2500.00", 1, "2500.00", true
        );

        jdbcTemplate.update("""
                INSERT INTO pedido_revision
                    (pedido_id, observacion, fecha_revision)
                VALUES (?, ?, ?)
                """,
                2L,
                "Pedido verificado",
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(2026, 11, 21, 12, 0)
                )
        );

        jdbcTemplate.update("""
                INSERT INTO pedido_revision
                    (pedido_id, observacion, fecha_revision)
                VALUES (?, ?, ?)
                """,
                3L,
                "Datos correctos",
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(2026, 11, 22, 12, 0)
                )
        );
    }

    private void insertarPedido(
            long id,
            String cliente,
            String codigo,
            String nombre,
            String precio,
            int cantidad,
            String total,
            boolean revisado
    ) {
        jdbcTemplate.update("""
                INSERT INTO pedido (
                    id, cliente, producto_codigo, producto_nombre,
                    precio_unitario, cantidad, total, fecha, estado, revisado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                cliente,
                codigo,
                nombre,
                new BigDecimal(precio),
                cantidad,
                new BigDecimal(total),
                java.sql.Timestamp.valueOf(
                        LocalDateTime.of(2026, 11, 20, 10, 0)
                ),
                "CONFIRMADO",
                revisado
        );
    }

    @Test
    void buscarPorId_pedidoExistente_devuelvePedido() {
        Optional<Pedido> resultado = repository.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().id());
        assertEquals("Cliente A", resultado.get().cliente());
        assertEquals("LAP-001", resultado.get().productoCodigo());

        assertEquals(
                0,
                new BigDecimal("5000.00").compareTo(resultado.get().total())
        );

        assertFalse(resultado.get().revisado());
    }

    @Test
    void buscarPorId_pedidoInexistente_devuelveOptionalVacio() {
        Optional<Pedido> resultado = repository.buscarPorId(999L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void listarTodos_devuelvePedidosOrdenadosDescendentemente() {
        List<Pedido> pedidos = repository.listarTodos();

        assertEquals(3, pedidos.size());
        assertEquals(3L, pedidos.get(0).id());
        assertEquals(2L, pedidos.get(1).id());
        assertEquals(1L, pedidos.get(2).id());
    }

    @Test
    void obtenerResumen_calculaIndicadoresCorrectamente() {
        ResumenPedidosProjection resultado =
                repository.obtenerResumen();

        assertEquals(3L, resultado.totalPedidos());
        assertEquals(6L, resultado.totalUnidades());

        assertEquals(
                0,
                new BigDecimal("10050.00")
                        .compareTo(resultado.importeAcumulado())
        );

        assertEquals(
                0,
                new BigDecimal("3350.00")
                        .compareTo(resultado.ticketPromedio())
        );
    }

    @Test
    void buscarPorCliente_filtraPorNombreParcial() {
        List<Pedido> resultado =
                repository.buscarPorCliente("Cliente A");

        assertEquals(2, resultado.size());

        assertTrue(resultado.stream()
                .allMatch(p -> p.cliente().equals("Cliente A")));
    }

    @Test
    void listarProductosMasVendidos_agrupaPorProducto() {
        List<ProductoVendidoProjection> resultado =
                repository.listarProductosMasVendidos();

        assertEquals(2, resultado.size());

        ProductoVendidoProjection primero = resultado.get(0);

        assertEquals("LAP-001", primero.productoCodigo());
        assertEquals("Laptop", primero.productoNombre());
        assertEquals(2L, primero.totalPedidos());
        assertEquals(3L, primero.totalUnidades());

        assertEquals(
                0,
                new BigDecimal("7500.00")
                        .compareTo(primero.importeAcumulado())
        );

        ProductoVendidoProjection segundo = resultado.get(1);

        assertEquals("MON-001", segundo.productoCodigo());
        assertEquals(3L, segundo.totalUnidades());
    }

    @Test
    void listarPorEstadoRevision_devuelveSoloPendientes() {
        List<PedidoRevisionProjection> resultado =
                repository.listarPorEstadoRevision(false);

        assertEquals(1, resultado.size());

        PedidoRevisionProjection revision = resultado.get(0);

        assertEquals(1L, revision.pedido().id());
        assertFalse(revision.pedido().revisado());
        assertNull(revision.observacion());
        assertNull(revision.fechaRevision());
    }

    @Test
    void listarPorEstadoRevision_devuelveRevisadosConTrazabilidad() {
        List<PedidoRevisionProjection> resultado =
                repository.listarPorEstadoRevision(true);

        assertEquals(2, resultado.size());

        PedidoRevisionProjection primera = resultado.get(0);

        assertEquals(3L, primera.pedido().id());
        assertTrue(primera.pedido().revisado());
        assertEquals("Datos correctos", primera.observacion());
        assertNotNull(primera.fechaRevision());

        PedidoRevisionProjection segunda = resultado.get(1);

        assertEquals(2L, segunda.pedido().id());
        assertEquals("Pedido verificado", segunda.observacion());
    }

    @Test
    void obtenerEstadisticasRevisiones_calculaPorcentajes() {
        EstadisticasRevisionesProjection resultado =
                repository.obtenerEstadisticasRevisiones();

        assertEquals(3L, resultado.totalPedidos());
        assertEquals(2L, resultado.totalRevisados());
        assertEquals(1L, resultado.totalPendientes());

        assertEquals(
                0,
                new BigDecimal("66.67")
                        .compareTo(resultado.porcentajeRevisado())
        );
    }
}
