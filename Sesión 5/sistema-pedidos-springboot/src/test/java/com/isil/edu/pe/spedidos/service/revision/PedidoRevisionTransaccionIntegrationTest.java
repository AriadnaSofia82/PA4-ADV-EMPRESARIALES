package com.isil.edu.pe.spedidos.service.revision;

import com.isil.edu.pe.spedidos.controller.dto.PedidoRevisionRequest;
import com.isil.edu.pe.spedidos.controller.dto.PedidoRevisionResponse;
import com.isil.edu.pe.spedidos.exception.PedidoRevisionException;
import com.isil.edu.pe.spedidos.repository.revision.PedidoRevisionJdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = PedidoRevisionTransaccionIntegrationTest.TestConfig.class
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PedidoRevisionTransaccionIntegrationTest {

    private final JdbcTemplate jdbcTemplate;
    private final PedidoRevisionService service;

    @Autowired
    PedidoRevisionTransaccionIntegrationTest(
            JdbcTemplate jdbcTemplate,
            PedidoRevisionService service
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.service = service;
    }

    @BeforeEach
    void prepararDatos() {

        jdbcTemplate.update("DELETE FROM pedido_revision");
        jdbcTemplate.update("DELETE FROM pedido");

        jdbcTemplate.update("""
                INSERT INTO pedido (id, revisado, estado)
                VALUES (?, ?, ?)
                """,
                1L,
                false,
                "REGISTRADO"
        );
    }

    @Test
    void debeConfirmarRevisionConDosEscrituras() {

        // Arrange
        PedidoRevisionRequest request = new PedidoRevisionRequest();
        request.setPedidoId(1L);
        request.setObservacion("Pedido revisado correctamente");

        // Act
        PedidoRevisionResponse resultado =
                service.registrarRevision(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getPedidoId());
        assertEquals(
                "Pedido revisado correctamente",
                resultado.getObservacion()
        );

        Boolean revisado = jdbcTemplate.queryForObject(
                "SELECT revisado FROM pedido WHERE id = ?",
                Boolean.class,
                1L
        );

        Integer cantidadRevisiones = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM pedido_revision
                WHERE pedido_id = ?
                """,
                Integer.class,
                1L
        );

        assertTrue(revisado);
        assertEquals(1, cantidadRevisiones);
    }

    @Test
    void debeRevertirActualizacionSiFallaInsercionRevision() {

        // Arrange
        PedidoRevisionRequest request = new PedidoRevisionRequest();
        request.setPedidoId(1L);
        request.setObservacion("FORZAR_ERROR");

        // Act
        assertThrows(
                PedidoRevisionException.class,
                () -> service.registrarRevision(request)
        );

        // Assert
        Boolean revisado = jdbcTemplate.queryForObject(
                "SELECT revisado FROM pedido WHERE id = ?",
                Boolean.class,
                1L
        );

        Integer cantidadRevisiones = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM pedido_revision
                WHERE pedido_id = ?
                """,
                Integer.class,
                1L
        );

        assertFalse(revisado);
        assertEquals(0, cantidadRevisiones);
    }

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {

        @Bean
        EmbeddedDatabase embeddedDatabase() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .addScript("schema.sql")
                    .build();
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        PlatformTransactionManager transactionManager(
                DataSource dataSource
        ) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        PedidoRevisionJdbcRepository pedidoRevisionJdbcRepository(
                JdbcTemplate jdbcTemplate
        ) {
            return new PedidoRevisionJdbcRepository(jdbcTemplate);
        }

        @Bean
        PedidoRevisionServiceImpl pedidoRevisionService(
                PedidoRevisionJdbcRepository repository
        ) {
            return new PedidoRevisionServiceImpl(repository);
        }
    }
}
