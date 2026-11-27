# Sistema de Pedidos

## Desarrollo de Aplicaciones Empresariales Avanzado | ISIL 2026-20

Sistema de gestión de pedidos desarrollado con **Java y Spring Boot**, como evolución de las entregas PA1, PA2, PA3 y PA4. La aplicación conserva las operaciones principales de pedidos y su integración HTTP con `inventario-service`. Además, incorpora un módulo de consulta y supervisión de pedidos, una API REST/JSON, seguridad basada en roles y un proceso transaccional para registrar la revisión de pedidos.

Este README reúne la información funcional y técnica de las entregas PA3 y PA4 en un único documento. Se distinguen las funcionalidades que pertenecen al módulo de reportes integrado en la aplicación y las que corresponden al servicio externo de reportes.

## 1. Funcionalidades y alcance

### 1.1. Gestión de pedidos

- Gestionar las operaciones principales de pedidos.
- Mantener las funcionalidades existentes de registro y anulación.
- Conservar la persistencia de las operaciones existentes mediante Spring Data JPA.
- Mantener la integración HTTP con `inventario-service` para validar o reservar inventario al registrar pedidos.
- Controlar de forma adecuada los casos en que el servicio de inventario no se encuentre disponible.

### 1.2. Consulta y supervisión de pedidos (PA3)

- Buscar pedidos por cliente y monto mínimo.
- Consultar un reporte de clientes frecuentes.
- Mostrar los resultados mediante una vista web JSP.
- Exponer las consultas mediante una API REST que devuelve JSON.
- Implementar las dos consultas nuevas de reportes con Spring JDBC, sin reemplazar la persistencia JPA de las operaciones existentes.
- Proteger el acceso mediante Spring Security y roles.

### 1.3. Revisión transaccional de pedidos (PA4)

- Registrar la revisión de un pedido mediante `PedidoRevisionService` y `PedidoRevisionServiceImpl`.
- Validar que el pedido exista, que la observación sea válida y que el pedido no haya sido revisado previamente.
- Actualizar `pedido.revisado` e insertar la trazabilidad en `pedido_revision` dentro de una misma transacción.
- Evitar revisiones duplicadas mediante validaciones de negocio y una restricción `UNIQUE` sobre `pedido_revision.pedido_id`.
- Proporcionar una interfaz web de supervisión mediante la vista JSP `revision-pedidos.jsp` y sus controladores.
- Mantener las funcionalidades anteriores de pedidos y anulación.

## 2. Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje y plataforma de ejecución |
| Spring Boot 4.1.1 | Configuración y ejecución de la aplicación, según la configuración documentada de PA4 |
| Spring Web / Spring MVC | Controladores web, navegación y endpoints |
| Spring Data JPA | Persistencia de las operaciones existentes de pedidos |
| Spring JDBC | Consultas SQL específicas del módulo de reportes |
| `NamedParameterJdbcTemplate` | Ejecución de consultas SQL parametrizadas |
| Spring Security | Autenticación, autorización por roles y protección CSRF |
| JSP / JSTL | Renderizado de las vistas web |
| MySQL 8.x | Base de datos para la ejecución normal |
| H2 | Base de datos utilizada en pruebas automatizadas |
| Maven / Maven Wrapper | Gestión de dependencias, compilación y ejecución de pruebas |
| JUnit 5 | Pruebas automatizadas |
| Mockito | Aislamiento de servicios y controladores durante las pruebas |
| Postman | Verificación de endpoints y escenarios HTTP |

También se utilizan clientes HTTP para la comunicación con los servicios externos, entre ellos `InventarioRestClient`.

## 3. Arquitectura y organización

La aplicación mantiene una arquitectura por capas y separación de responsabilidades:

```text
Navegador / Postman
        |
        v
  Spring Security
 (autenticación, roles y CSRF)
        |
        v
  DispatcherServlet
        |
        +-------------------------------+
        |                               |
        v                               v
Controladores web                 Controladores REST
(@Controller)                     (@RestController)
        |                               |
        +---------------+---------------+
                        |
                        v
                  Capa Service
                        |
          +-------------+--------------+
          |                            |
          v                            v
 Repositorios JPA              Repositorio JDBC
 operaciones de pedidos       consultas de reportes
          |                            |
          +-------------+--------------+
                        |
                        v
                      MySQL

Sistema de pedidos
   |                         |
   v                         v
inventario-service     pedidos-reportes-service
```

La gestión habitual de pedidos continúa utilizando Spring Data JPA. Las consultas nuevas de PA3 se implementan con Spring JDBC. La integración con inventario se mantiene mediante el cliente HTTP correspondiente.

### 3.1. Componentes del módulo de reportes (PA3)

- **`PedidoReporteWebController`**: atiende la ruta web de reportes, prepara los datos mediante `Model` y retorna la vista JSP.
- **`PedidoApiController`**: publica los endpoints JSON y delega las operaciones en la capa Service.
- **`PedidoReporteService` / `PedidoReporteServiceImpl`**: centralizan las operaciones y validaciones de negocio.
- **`PedidoReporteJdbcRepository`**: contiene las consultas SQL implementadas con Spring JDBC.
- **`PedidoRowMapper`**: convierte las filas del resultado en objetos `Pedido`, cuando corresponde.
- **Proyecciones/DTO de reporte**: representan los resultados agregados de clientes frecuentes.

La vista JSP y la API REST utilizan la misma capa Service. Los controladores no duplican las consultas SQL ni las reglas de negocio.

### 3.2. Componentes de revisión (PA4)

- **`PedidoRevisionService`**: define la operación de revisión.
- **`PedidoRevisionServiceImpl`**: implementa las validaciones y el registro transaccional.
- **Controladores de revisión**: exponen la operación y atienden la interfaz de supervisión.
- **`revision-pedidos.jsp`**: vista web para la supervisión de revisiones.
- **`pedido_revision`**: tabla que conserva la trazabilidad de la revisión de cada pedido.

Los nombres y subpaquetes concretos pueden variar según la estructura actual del repositorio.

## 4. Funcionalidades del módulo de reportes

### 4.1. Búsqueda avanzada de pedidos

Se incorporó la consulta:

```text
buscarPedidosPorClienteYTotalMinimo(cliente, totalMinimo)
```

Características:

- Busca pedidos correspondientes al cliente indicado.
- Filtra los pedidos cuyo total sea igual o superior al monto mínimo.
- Utiliza `NamedParameterJdbcTemplate`.
- Envía los valores como parámetros con nombre; no concatena los datos del usuario al SQL.
- Reutiliza `PedidoRowMapper` para convertir los resultados en objetos `Pedido`.
- Ordena los resultados por identificador descendente.
- Valida en la capa Service que el cliente no esté vacío y que el monto mínimo no sea negativo.

### 4.2. Reporte de clientes frecuentes

Se incorporó la consulta:

```text
listarClientesFrecuentes(minPedidos)
```

Características:

- Utiliza SQL con `GROUP BY` y `HAVING`.
- Filtra los clientes que alcanzan el mínimo de pedidos indicado.
- Devuelve los siguientes indicadores por cliente:
  - `totalPedidos`: cantidad de pedidos.
  - `totalUnidades`: unidades acumuladas.
  - `importeAcumulado`: importe total de los pedidos.
  - `ticketPromedio`: importe promedio por pedido.
- Utiliza una proyección o DTO para representar los datos agregados.
- Valida que `minPedidos` sea mayor o igual a 1 antes de consultar.

### 4.3. Vista web de reportes

Ruta:

```http
GET /reportes/pedidos
```

Vista:

```text
/WEB-INF/views/reporte-pedidos.jsp
```

El controlador utiliza `@Controller`, `@GetMapping` y `Model`, y retorna la vista mediante el ViewResolver configurado para JSP. La información se obtiene desde la capa Service.

### 4.4. API REST / JSON de reportes

Considerando el context path `/sistema-pedidos`:

| Método | Ruta | Parámetros | Finalidad |
|---|---|---|---|
| GET | `/api/v1/pedidos/busqueda-avanzada` | `cliente`, `totalMinimo` | Buscar pedidos por cliente y monto mínimo |
| GET | `/api/v1/pedidos/clientes-frecuentes` | `minPedidos` | Consultar clientes que cumplen el umbral de pedidos |

Ejemplos:

**Búsqueda avanzada**

```http
GET http://localhost:8081/sistema-pedidos/api/v1/pedidos/busqueda-avanzada?cliente=Juan&totalMinimo=100
```

**Clientes frecuentes**

```http
GET http://localhost:8081/sistema-pedidos/api/v1/pedidos/clientes-frecuentes?minPedidos=2
```

Los endpoints responden JSON y utilizan la misma capa Service que la vista JSP. Los parámetros inválidos deben manejarse de forma controlada mediante respuestas HTTP coherentes.

## 5. Revisión transaccional de pedidos

La operación de registro de revisión se expone desde el sistema principal:

```http
POST http://localhost:8081/sistema-pedidos/revision
```

El flujo de negocio contempla:

1. Buscar el pedido y validar su existencia.
2. Validar la observación.
3. Comprobar que el pedido no haya sido revisado.
4. Actualizar `pedido.revisado` a `1`.
5. Insertar `pedido_id`, `observacion` y `fecha_revision` en `pedido_revision`.
6. Devolver el resultado de la revisión.

La operación utiliza control transaccional (`@Transactional(rollbackFor = Exception.class)`, según la implementación documentada). Si falla una de las operaciones de persistencia, los cambios deben revertirse conjuntamente. La restricción única de `pedido_revision.pedido_id` refuerza la prevención de registros duplicados.

El contrato exacto del JSON de entrada y salida debe consultarse en el DTO y el controlador vigentes del proyecto.

## 6. Integración con servicios externos

### 6.1. `inventario-service`

El sistema principal consume `inventario-service` para las operaciones de inventario relacionadas con el registro de pedidos.

URL base utilizada en el entorno local:

```text
http://localhost:8082/inventario-service/api/inventario
```

La disponibilidad de este servicio es necesaria para probar el registro de pedidos que requiere validar o reservar stock. Si el servicio no está disponible, la aplicación debe informar el problema de forma controlada. Las consultas de reportes almacenados localmente deben poder continuar funcionando.

### 6.2. `pedidos-reportes-service`

La aplicación también contempla la integración con el servicio independiente de reportes para consultas de supervisión integradas.

URL base local documentada:

```text
http://localhost:8083/pedidos-reportes-service/api/v1/pedidos
```

Este servicio es de lectura: el registro o modificación de una revisión se realiza en la aplicación principal. Esta integración externa es distinta del módulo de reportes PA3, cuyas consultas JDBC se ejecutan desde la propia aplicación sobre la base de datos local.

## 7. Seguridad: Spring Security

La configuración `SecurityConfig` controla el acceso al módulo de reportes y conserva la protección de las operaciones de pedidos.

### 7.1. Roles de laboratorio

| Usuario | Rol | Permisos |
|---|---|---|
| `operador` | `OPERADOR` | Consultar reportes y registrar pedidos |
| `consulta` | `CONSULTA` | Consultar reportes; no puede registrar pedidos |

Los usuarios de laboratorio están configurados en memoria, de acuerdo con el alcance de la actividad. No se requiere implementar JWT, OAuth2/OIDC ni persistencia de usuarios en base de datos.

### 7.2. Reglas de acceso

- Los usuarios no autenticados que ingresen a `/reportes/pedidos` deben pasar por el mecanismo de autenticación configurado.
- Los roles `OPERADOR` y `CONSULTA` pueden consultar `/reportes/**`.
- Los roles `OPERADOR` y `CONSULTA` pueden consultar los endpoints de reportes `/api/v1/pedidos/**`.
- El registro mediante `POST /pedidos` está reservado al rol `OPERADOR`.
- Se conserva la protección CSRF. No se deshabilita globalmente para permitir solicitudes POST.

Las contraseñas de laboratorio deben consultarse en la configuración local del proyecto. No se incluyen credenciales en este README. No publicar contraseñas personales, tokens ni claves reales.

## 8. Configuración del entorno

### 8.1. Requisitos previos

- JDK 21 configurado en `JAVA_HOME`.
- MySQL 8.x en ejecución.
- Base de datos `InventarioDB` creada y con las tablas requeridas por el sistema, de acuerdo con la configuración documentada de PA4.
- Maven o Maven Wrapper, si está incluido en el repositorio.
- `inventario-service` disponible para probar el registro de pedidos.
- `pedidos-reportes-service` disponible cuando se utilicen las consultas de supervisión integradas.
- Postman u otro cliente HTTP para probar los endpoints.
- PowerShell o una terminal compatible.
- IDE recomendado: IntelliJ IDEA o Visual Studio Code.

### 8.2. Base de datos MySQL

La ejecución normal utiliza MySQL. Para el entorno PA3 se documenta el script:

```text
Script_BD_usuario_MySql.sql
```

Ejecutar el script en MySQL antes de iniciar la aplicación, cuando corresponda a la configuración del repositorio. Verificar que la base de datos exista y que el usuario tenga permisos sobre ella.

La configuración local de PA4 registra la base de datos `InventarioDB`. La base de datos y el esquema efectivos deben coincidir con el archivo `application.properties` o `application.yml` vigente.

### 8.3. Variables de entorno

Las credenciales y la URL de conexión deben mantenerse fuera del repositorio. Las variables documentadas son:

| Variable | Descripción |
|---|---|
| `DB_URL` | URL JDBC de la base de datos MySQL |
| `DB_USERNAME` | Usuario de MySQL |
| `DB_PASSWORD` | Contraseña del usuario de MySQL |

Ejemplo para PowerShell (reemplazar los valores por los del entorno local):

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/InventarioDB?serverTimezone=America/Lima"
$env:DB_USERNAME="USUARIO_MYSQL"
$env:DB_PASSWORD="CONTRASENA_LOCAL"
```

La aplicación debe mapear estas variables desde `application.properties` o `application.yml`. Los nombres de las propiedades de Spring deben coincidir con las referencias `${...}` utilizadas en dichos archivos.

**Importante:** no publicar contraseñas, tokens ni archivos `.env` reales. Si el proyecto utiliza nombres de variables o una base de datos diferentes, prevalece la configuración efectiva del repositorio.

### 8.4. Base de datos para pruebas

H2 se reserva para las pruebas automatizadas. Las pruebas de repositorio e integración de acceso a datos deben utilizar datos aislados y no depender de la base MySQL de ejecución normal.

### 8.5. URLs de servicios

| Servicio | URL local documentada |
|---|---|
| Sistema de pedidos | `http://localhost:8081/sistema-pedidos` |
| Inventario | `http://localhost:8082/inventario-service/api/inventario` |
| Reportes externo | `http://localhost:8083/pedidos-reportes-service/api/v1/pedidos` |

Los puertos, context paths y URLs pueden variar si se modifica la configuración local. Verificar siempre los valores efectivos en los archivos de configuración.

## 9. Ejecución de la aplicación

### 9.1. Orden de arranque recomendado

1. Iniciar MySQL y verificar que la base de datos y las tablas requeridas estén disponibles.
2. Configurar `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.
3. Iniciar `inventario-service` si se realizarán operaciones que requieren stock.
4. Iniciar `pedidos-reportes-service` si se utilizará la supervisión integrada que depende de ese servicio.
5. Iniciar `sistema-pedidos-springboot`.

Las consultas del módulo de reportes PA3 acceden a la base de datos local y no dependen de que `inventario-service` esté disponible.

### 9.2. Ejecutar con Maven Wrapper

Desde la carpeta raíz del proyecto, en PowerShell:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Para compilar el paquete:

```powershell
.\mvnw.cmd clean package
```

Si el repositorio no incluye Maven Wrapper, utilizar Maven instalado:

```bash
mvn clean package
mvn spring-boot:run
```

También puede ejecutarse desde el IDE utilizando la clase principal anotada con `@SpringBootApplication`.

### 9.3. Dirección local

La configuración documentada para el sistema principal es:

- Puerto: `8081`.
- Context path: `/sistema-pedidos`.
- URL base: `http://localhost:8081/sistema-pedidos`.

## 10. Rutas principales

Considerando el puerto `8081` y el context path `/sistema-pedidos`:

| Recurso | URL |
|---|---|
| Aplicación | `http://localhost:8081/sistema-pedidos` |
| Vista de reportes PA3 | `http://localhost:8081/sistema-pedidos/reportes/pedidos` |
| API de búsqueda avanzada | `http://localhost:8081/sistema-pedidos/api/v1/pedidos/busqueda-avanzada?cliente=Juan&totalMinimo=100` |
| API de clientes frecuentes | `http://localhost:8081/sistema-pedidos/api/v1/pedidos/clientes-frecuentes?minPedidos=2` |
| Registro de revisión | `POST http://localhost:8081/sistema-pedidos/revision` |

Para las rutas de registro de pedidos y demás operaciones preexistentes, consultar los controladores y la configuración efectiva del proyecto.

## 11. Pruebas automatizadas

Las pruebas deben utilizar JUnit 5 y Mockito cuando corresponda, aplicar el patrón AAA (Arrange – Act – Assert) y utilizar H2 para las pruebas de integración del acceso a datos.

### 11.1. Clases de prueba documentadas para PA3

| Clase de prueba | Alcance |
|---|---|
| `PedidoApiControllerTest` | Comportamiento del controlador API y validaciones/respuestas |
| `PedidoReporteControllerIntegrationTest` | Integración de la ruta web de reportes |
| `PedidoSecurityIntegrationTest` | Autenticación, autorización y reglas de seguridad |
| `PedidoServiceImplTest` | Comportamiento del servicio de pedidos y continuidad con inventario |
| `PedidoReporteServiceTest` | Lógica y validaciones del servicio de reportes |

### 11.2. Escenarios de revisión PA4

Las pruebas de la revisión deben cubrir, según las clases implementadas:

- Registro correcto de revisión.
- Pedido inexistente.
- Observación inválida.
- Revisión duplicada.
- Rollback ante un fallo durante la operación transaccional.
- Colaboración entre servicios y repositorios.

El informe PA4 registra una ejecución de **56 pruebas, 56 exitosas, 0 fallidas y 0 omitidas** para esta aplicación. Este resultado corresponde a la ejecución documentada en esa entrega; debe volver a comprobarse si cambia el código.

### 11.3. Ejecutar las pruebas

Con Maven Wrapper:

```powershell
.\mvnw.cmd clean test
```

También se puede ejecutar el ciclo de verificación:

```powershell
.\mvnw.cmd clean verify
```

Con Maven instalado:

```bash
mvn test
mvn clean verify
```

El resultado esperado de una ejecución correcta es `BUILD SUCCESS`. Revisar el resumen de Maven y los reportes generados en:

```text
target/surefire-reports/
```

La presencia de las clases de prueba no sustituye su ejecución. Antes de entregar, ejecutar las pruebas y registrar el resultado real. No declarar pruebas exitosas si la última ejecución no terminó correctamente.

## 12. Escenarios funcionales de verificación (PA3)

Los siguientes escenarios deben poder reproducirse y documentarse con evidencias: parámetros, respuesta, código HTTP y, cuando corresponda, consulta o datos de MySQL.

### Caso 1. Búsqueda por cliente y monto mínimo

1. Registrar al menos tres pedidos.
2. Preparar datos donde solo una parte cumpla simultáneamente el cliente y el monto mínimo.
3. Consultar la vista `/reportes/pedidos`.
4. Consultar el endpoint `/api/v1/pedidos/busqueda-avanzada`.
5. Comparar los pedidos mostrados con los registros de MySQL.

**Resultado esperado:** únicamente aparecen los pedidos que cumplen ambos filtros, ordenados por identificador descendente.

### Caso 2. Clientes frecuentes y consolidado

1. Generar pedidos para al menos dos clientes.
2. Consultar el endpoint de clientes frecuentes con `minPedidos=2`.
3. Revisar los valores de `totalPedidos`, `totalUnidades`, `importeAcumulado` y `ticketPromedio`.
4. Contrastar los resultados con los datos de MySQL.

**Resultado esperado:** solo aparecen los clientes que alcanzan el umbral y sus indicadores agregados son correctos.

### Caso 3. Parámetros inválidos

Probar los siguientes valores:

```text
cliente=
totalMinimo=-1
minPedidos=0
```

**Resultado esperado:** la aplicación rechaza los parámetros inválidos de forma controlada (por ejemplo, HTTP 400), sin ejecutar consultas construidas mediante concatenación insegura.

### Caso 4. Continuidad con PA2: inventario indisponible

1. Detener `inventario-service`.
2. Intentar registrar un pedido nuevo.
3. Comprobar que la aplicación informa la indisponibilidad de inventario de forma controlada.
4. Sin reiniciar el sistema de pedidos, ingresar al módulo de reportes y consultar pedidos previamente almacenados.

**Resultado esperado:** el registro no se completa si no se puede validar o reservar inventario, pero los reportes locales continúan funcionando porque consultan la base de datos del sistema de pedidos.

### Caso 5. Seguridad por roles y CSRF

1. Acceder como usuario anónimo a `/reportes/pedidos`: debe solicitar autenticación o redirigir al mecanismo configurado.
2. Iniciar sesión como `CONSULTA`: debe poder visualizar los reportes.
3. Intentar registrar un pedido como `CONSULTA`, incluso enviando un token CSRF válido: debe ser rechazado por autorización.
4. Intentar un POST como `OPERADOR` sin token CSRF: debe ser rechazado por la protección CSRF.
5. Restaurar `inventario-service` para la evidencia funcional final de registro.

**Resultado esperado:** se respetan los permisos por rol y CSRF permanece habilitado.

## 13. Postman y evidencias

La colección de Postman del proyecto permite reproducir las solicitudes de la API. Para la entrega, documentar como mínimo:

- Método y URL solicitada.
- Parámetros enviados.
- Usuario o rol utilizado cuando corresponda.
- Código HTTP recibido.
- Respuesta JSON o resultado de la vista.
- Comparación con MySQL cuando el caso lo requiera.

La colección debe mantenerse actualizada con las rutas del context path y los endpoints de reportes. Para las operaciones protegidas, iniciar sesión y conservar el mecanismo de sesión/cookies y el token CSRF requerido por la configuración de seguridad.

Las capturas deben complementar, no reemplazar, el repositorio, el código y la ejecución reproducible de las pruebas.

## 14. Repositorio y control de versiones

Completar antes de presentar la entrega:

- **Repositorio:** `https://github.com/AriadnaSofia82/PA4-ADV-EMPRESARIALES.git`
- **Rama evaluada:** `MASTER`
- **Commit final:** `PEGAR_HASH_DEL_COMMIT`

El repositorio debe permitir revisar el código fuente, los commits y las pruebas. No se considera suficiente entregar únicamente un ZIP o capturas de pantalla.

No versionar:

- `target/`.
- Archivos de configuración locales con credenciales.
- Archivos `.env` con secretos.
- Logs, archivos temporales o datos locales del IDE.

Sí versionar el código fuente, `pom.xml`, Maven Wrapper, pruebas y documentación.

## 15. Limitaciones y consideraciones

- MySQL es la base de datos de ejecución normal; H2 se utiliza para las pruebas automatizadas.
- `inventario-service` es una dependencia externa para el registro de pedidos. Su indisponibilidad no debe impedir consultar los reportes almacenados localmente.
- El módulo de reportes PA3 ejecuta sus consultas JDBC desde la aplicación principal; `pedidos-reportes-service` es un servicio externo adicional utilizado para consultas de supervisión integradas.
- Los usuarios de laboratorio de Spring Security están configurados en memoria; no se implementa persistencia de usuarios.
- No se implementan JWT, OAuth2/OIDC ni una solución de autenticación externa, porque no forman parte del alcance documentado.
- La protección CSRF debe permanecer activa.
- El registro de revisión se realiza en la aplicación principal y debe mantener la atomicidad de la actualización del pedido y la inserción de su trazabilidad.
- No se deben subir credenciales reales, tokens ni archivos sensibles.
- Antes de la entrega, verificar la ejecución de `mvn test`, la disponibilidad del repositorio, las rutas y puertos configurados y las evidencias de los escenarios funcionales.

## 16. Referencias

- Spring. (s. f.). *Spring Boot Reference Documentation*. https://docs.spring.io/spring-boot/
- Spring. (s. f.). *Spring Framework Reference Documentation*. https://docs.spring.io/spring-framework/reference/
- Spring. (s. f.). *Spring Security Reference*. https://docs.spring.io/spring-security/reference/
- Spring. (s. f.). *Spring Data JPA Reference Documentation*. https://docs.spring.io/spring-data/jpa/reference/
- Spring. (s. f.). *JDBC*. https://docs.spring.io/spring-framework/reference/data-access/jdbc.html
- Apache Maven. (s. f.). *Maven Documentation*. https://maven.apache.org/guides/
- JUnit. (s. f.). *JUnit 5 User Guide*. https://junit.org/junit5/docs/current/user-guide/
- MySQL. (s. f.). *MySQL Reference Manual*. https://dev.mysql.com/doc/

---

**Documento unificado de apoyo para la ejecución y revisión técnica del Sistema de Pedidos (PA3 y PA4).** Antes de publicar el repositorio, completar los datos de Git y comprobar que los puertos, rutas, usuarios de laboratorio y variables de entorno coincidan con la configuración efectiva del proyecto.
