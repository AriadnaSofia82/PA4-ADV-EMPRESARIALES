# pedidos-reportes-service

Servicio independiente de consulta y supervisión del ecosistema
**Sistema de Pedidos -- PA4**. Expone una API REST/JSON para consultar
pedidos pendientes, pedidos revisados y estadísticas de revisión. Su
responsabilidad es de lectura: no registra ni modifica revisiones.

## 1. Responsabilidades

-   Consultar pedidos pendientes de revisión.
-   Consultar pedidos que ya cuentan con una revisión registrada.
-   Obtener estadísticas consolidadas del proceso de revisión.
-   Consultar la información persistida en `InventarioDB`.
-   Exponer resultados mediante DTOs y proyecciones, sin acoplar la API
    directamente a las entidades persistentes.
-   Mantener la separación Controller → Service → Repository.

El registro de revisiones se realiza en `sistema-pedidos-springboot`, no
en este servicio.

## 2. Tecnologías

-   Java 21.
-   Spring Boot 4.1.1 (según la configuración documentada del proyecto).
-   Spring Web.
-   Spring JDBC.
-   `NamedParameterJdbcTemplate`.
-   MySQL 8.x.
-   Maven Wrapper.
-   JUnit 5 y Mockito.

## 3. Requisitos previos

-   JDK 21 configurado en `JAVA_HOME`.
-   MySQL 8.x en ejecución.
-   Base de datos `InventarioDB` accesible.
-   Tablas `pedido` y `pedido_revision` creadas y con la estructura
    esperada por las consultas.
-   `sistema-pedidos-springboot` disponible para validar el flujo
    completo de integración.

## 4. Configuración de base de datos

La configuración local documentada utiliza MySQL y la base
`InventarioDB`:

``` text
jdbc:mysql://localhost:3306/InventarioDB?serverTimezone=America/Lima
```

Parámetros de entorno contemplados para la solución:

``` properties
DB_URL=jdbc:mysql://localhost:3306/InventarioDB?serverTimezone=America/Lima
DB_USERNAME=<usuario_mysql>
DB_PASSWORD=<contraseña_mysql>
```

Configurar el datasource en `application.properties` o `application.yml`
de acuerdo con los nombres de propiedades que realmente utilice el
proyecto. No publicar credenciales reales. Si el archivo de
configuración vigente define otros nombres, esos nombres son los que
deben establecerse en el entorno.

## 5. Ejecución

Desde la carpeta del proyecto, en PowerShell:

``` powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Configuración local documentada:

-   Puerto: `8083`.
-   Context path: `/pedidos-reportes-service`.
-   URL base:
    `http://localhost:8083/pedidos-reportes-service/api/v1/pedidos`.

## 6. Endpoints REST

Base:

``` text
http://localhost:8083/pedidos-reportes-service/api/v1/pedidos
```

  --------------------------------------------------------------------------------
  Método                  Endpoint                         Descripción
  ----------------------- -------------------------------- -----------------------
  GET                     `/revisiones?estado=PENDIENTE`   Lista pedidos cuyo
                                                           indicador de revisión
                                                           está pendiente.

  GET                     `/revisiones?estado=REVISADO`    Lista pedidos que
                                                           cuentan con revisión
                                                           registrada.

  GET                     `/estadisticas/revisiones`       Devuelve estadísticas
                                                           consolidadas de
                                                           revisión.
  --------------------------------------------------------------------------------

Ejemplos completos:

``` http
GET http://localhost:8083/pedidos-reportes-service/api/v1/pedidos/revisiones?estado=PENDIENTE
```

``` http
GET http://localhost:8083/pedidos-reportes-service/api/v1/pedidos/revisiones?estado=REVISADO
```

``` http
GET http://localhost:8083/pedidos-reportes-service/api/v1/pedidos/estadisticas/revisiones
```

Las respuestas se entregan en JSON. La estructura exacta de los
atributos depende de los DTO/proyecciones vigentes en el código.

## 7. Acceso a datos

El proyecto utiliza `PedidoReporteJdbcRepository` para las consultas del
módulo. Entre las operaciones documentadas se encuentran:

-   `listarPorEstadoRevision(boolean)`: consulta pedidos según el
    indicador de revisión y utiliza un `LEFT JOIN` entre `pedido` y
    `pedido_revision`.
-   `obtenerEstadisticasRevisiones()`: consolida los indicadores
    estadísticos de revisión.

La consulta con `LEFT JOIN` permite incluir pedidos que todavía no
tienen una fila asociada en `pedido_revision`, así como aquellos que sí
cuentan con registro.

## 8. Pruebas automatizadas

Ejecutar desde PowerShell:

``` powershell
.\mvnw.cmd clean test
```

Las pruebas del servicio deben verificar:

-   Consulta de pedidos pendientes.
-   Consulta de pedidos revisados.
-   Cálculo de estadísticas.
-   Mapeo de resultados JDBC a DTOs/proyecciones.
-   Comportamiento de la capa Service y del controlador ante resultados
    y errores.

El informe PA4 registra una ejecución de **31 pruebas, 31 exitosas, 0
fallidas y 0 omitidas** para este proyecto. Este dato corresponde a la
ejecución documentada y debe actualizarse si cambia el código.

## 9. Estructura de capas

-   `controller`: `PedidoReporteController` y endpoints REST.
-   `service`: contrato y lógica de consulta.
-   `repository`: `PedidoReporteJdbcRepository`.
-   `controller.dto` / `service.domain`: DTOs y modelos de respuesta,
    según la estructura del proyecto.
-   `test`: pruebas unitarias y de integración.

No se incorpora un repositorio separado `PedidoRevisionJdbcRepository`;
las consultas de revisión se mantienen en `PedidoReporteJdbcRepository`,
según la decisión de diseño documentada.

## 10. Integración con el sistema principal

El sistema principal utiliza este servicio para recuperar información de
supervisión. El flujo de integración recomendado es:

1.  Consultar los pedidos pendientes.
2.  Seleccionar un pedido.
3.  Registrar su revisión desde `sistema-pedidos-springboot`.
4.  Volver a consultar pendientes y comprobar que ya no aparezca.
5.  Consultar revisados y comprobar que figure.
6.  Consultar estadísticas y contrastarlas con MySQL.

## 11. Consideraciones para GitHub

No versionar `target/`, credenciales, archivos `.env` reales ni logs. Sí
versionar el código fuente, `pom.xml`, Maven Wrapper, pruebas y
documentación.
