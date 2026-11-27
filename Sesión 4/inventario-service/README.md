# Inventario Service

## Sistema de Pedidos | Desarrollo de Aplicaciones Empresariales

Servicio de inventario que forma parte del ecosistema **Sistema de Pedidos**. Expone una API REST para consultar productos y administrar la disponibilidad de stock. Es consumido por `sistema-pedidos-springboot` durante las operaciones relacionadas con inventario.

Este README reúne la información técnica y de ejecución de los dos documentos proporcionados. La documentación más reciente describe una configuración basada en **Jakarta EE 11, WildFly 41 y MySQL 8.x**. Las diferencias entre ambos README se explican al final para evitar combinar configuraciones incompatibles.

## 1. Responsabilidades

- Exponer el catálogo de productos.
- Consultar productos y stock disponible por identificador o código.
- Registrar reservas de stock.
- Contemplar devoluciones o liberaciones de stock cuando estén implementadas en el recurso REST.
- Mantener la persistencia del inventario mediante el datasource del servicio.
- Proporcionar las operaciones de inventario al sistema principal mediante HTTP/JSON.

`inventario-service` administra productos y stock. `sistema-pedidos-springboot` gestiona los pedidos y consume esta API; no debe modificar directamente la tabla `producto_inventario`.

## 2. Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje y plataforma |
| Maven | Dependencias, compilación y pruebas |
| Jakarta EE 11 | Plataforma empresarial |
| WildFly 41 | Servidor de aplicaciones |
| Jakarta REST (JAX-RS) | Endpoints REST |
| Jakarta Enterprise Beans (EJB) | Servicios de negocio |
| Jakarta Persistence 3.2 / JPA | Persistencia |
| Hibernate | Implementación de JPA |
| Jakarta Transactions (JTA) | Transacciones |
| Bean Validation | Validación de datos |
| MySQL Connector/J | Conectividad con MySQL |
| MySQL 8.x | Base de datos |

Las versiones efectivas deben contrastarse con `pom.xml`, `persistence.xml` y la configuración del servidor del repositorio.

## 3. Arquitectura

La comunicación entre el sistema de pedidos y el servicio de inventario se realiza mediante HTTP y JSON. Los recursos REST delegan las operaciones en la capa de negocio, que accede a la persistencia mediante JPA.

```text
Sistema de Pedidos / Postman
          |
          | HTTP + JSON
          v
ProductoResource (Jakarta REST)
          |
          | @EJB
          v
InventarioService (@Stateless)
          |
          | @PersistenceContext
          v
EntityManager / InventarioPU
          |
          | java:jboss/datasources/InventarioDS
          v
MySQL — InventarioDB
```

Componentes funcionales:

- **Recursos REST (JAX-RS):** reciben solicitudes y exponen endpoints.
- **Servicios EJB:** coordinan las operaciones de inventario.
- **Persistencia JPA:** accede a los datos mediante `EntityManager`.
- **Datasource JNDI:** proporciona la conexión a la base de datos.
- **Entidades y DTO:** representan productos, stock y operaciones de reserva.

Los nombres exactos de paquetes y clases deben revisarse en el código fuente vigente.

## 4. Requisitos previos

1. JDK 21 instalado y configurado en `JAVA_HOME`.
2. Maven instalado y disponible desde la terminal.
3. MySQL Server 8.x instalado y en ejecución.
4. Base de datos `InventarioDB` creada con las tablas requeridas.
5. Usuario `inventario_app` creado en MySQL con permisos sobre las tablas utilizadas.
6. Puerto HTTP local `8082` disponible.
7. Datasource JNDI `InventarioDS` configurado en WildFly.

MySQL debe estar disponible en `localhost:3306`, salvo que se haya configurado otro host o puerto.

## 5. Preparación de la base de datos

Desde MySQL Workbench o la consola de MySQL, ejecutar el script de inicialización:

```sql
SOURCE ruta/al/proyecto/database/01_crear_inventario.sql;
```

Este script crea la base de datos, la tabla `producto_inventario`, el usuario y los productos de demostración, según su implementación.

| Archivo | Función |
|---|---|
| `database/01_crear_inventario.sql` | Creación inicial de base de datos, usuario, tabla y datos de demostración |
| `database/02_reset_stock_demo.sql` | Restablece el stock de demostración |
| `database/03_migrar_agregar_precio.sql` | Agrega o completa el campo `precio` en una tabla existente |

**Importante:** `02_reset_stock_demo.sql` sobrescribe las cantidades de stock con valores de demostración. No ejecutarlo si se desea conservar el stock actual.

## 6. Configuración de conexión

La conexión a MySQL se administra mediante un DataSource de WildFly. El archivo de configuración documentado es:

```text
config/database.properties
```

Datos de conexión:

| Parámetro | Valor |
|---|---|
| Motor | MySQL |
| Host | `localhost` |
| Puerto | `3306` |
| Base de datos | `InventarioDB` |
| Usuario | `inventario_app` |
| JNDI | `java:jboss/datasources/InventarioDS` |

El nombre JNDI debe coincidir con el declarado en `persistence.xml`. La contraseña debe coincidir con la configurada para el usuario MySQL.

En el README anterior aparece `java:/InventarioDS`. Para la configuración más reciente se documenta `java:jboss/datasources/InventarioDS`; utilizar el nombre que coincida realmente entre WildFly y `persistence.xml`.

## 7. Ejecución del proyecto

Abrir una terminal en la carpeta raíz de `inventario-service`.

### 7.1. Verificar Java y Maven

```bash
java -version
mvn -version
```

Comprobar que Java corresponda a la versión 21.

### 7.2. Ejecutar las pruebas

```bash
mvn clean test
```

Si el proyecto incluye Maven Wrapper:

```powershell
.\mvnw.cmd clean test
```

### 7.3. Compilar e iniciar WildFly

La documentación más reciente indica el uso del plugin de Maven:

```bash
mvn clean wildfly:run
```

Si se utiliza Maven Wrapper, se puede compilar con:

```powershell
.\mvnw.cmd clean package
```

Después, iniciar el servidor mediante el goal o script configurado en el `pom.xml` y los archivos del proyecto. La documentación anterior recomienda revisar primero los scripts y la configuración de ejecución. Por ello, confirmar que `wildfly:run` esté realmente configurado antes de utilizarlo; no asumir que el goal existe.

### 7.4. Puertos y dirección local

El entorno documentado utiliza `port-offset=2`:

| Servicio | Puerto |
|---|---:|
| HTTP | `8082` |
| Management | `9992` |

Aplicación:

```text
http://localhost:8082/inventario-service/
```

URL base de la API:

```text
http://localhost:8082/inventario-service/api/inventario
```

El desplazamiento de puertos permite ejecutar simultáneamente el sistema principal en el puerto `8081`, siempre que no existan conflictos.

## 8. API REST

Ruta base:

```text
http://localhost:8082/inventario-service/api/inventario
```

### 8.1. Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/productos` | Lista los productos registrados |
| GET | `/productos/{id}` | Consulta un producto por identificador |
| GET | `/productos/{id}/stock` | Consulta el stock por identificador |
| POST | `/productos/{id}/reservas` | Registra una reserva por identificador |
| GET | `/productos/codigo/{codigo}` | Consulta un producto por código |
| GET | `/productos/codigo/{codigo}/stock` | Consulta el stock por código |
| POST | `/productos/codigo/{codigo}/reservas` | Registra una reserva por código |

El proyecto también contempla operaciones de devolución o liberación de stock. Para no documentar una ruta o método HTTP incorrectos, consultar el recurso JAX-RS correspondiente y utilizar el path y verbo definidos en el código.

### 8.2. Consultar productos

```http
GET http://localhost:8082/inventario-service/api/inventario/productos
```

### 8.3. Reservar stock por ID

```http
POST http://localhost:8082/inventario-service/api/inventario/productos/1/reservas
Content-Type: application/json
```

Body:

```json
{
  "cantidad": 2
}
```

### 8.4. Reservar stock por código

```http
POST http://localhost:8082/inventario-service/api/inventario/productos/codigo/LAP-001/reservas
Content-Type: application/json
```

Body:

```json
{
  "cantidad": 2
}
```

Códigos de productos de demostración documentados:

- `LAP-001`: Laptop.
- `MON-001`: Monitor.
- `TEC-001`: producto adicional de demostración.

Los valores de stock pueden cambiar durante las pruebas; no deben considerarse datos fijos.

## 9. Integración con Sistema de Pedidos

El proyecto `sistema-pedidos-springboot` consume los endpoints REST de `inventario-service` para consultar y reservar stock durante las operaciones relacionadas con pedidos.

URL base utilizada por el cliente HTTP:

```text
http://localhost:8082/inventario-service/api/inventario
```

Antes de probar el registro de pedidos que reserva stock, verificar que:

1. MySQL esté disponible.
2. `InventarioDB` y las tablas requeridas existan.
3. El datasource `InventarioDS` esté correctamente configurado.
4. `inventario-service` haya iniciado sin errores.
5. La URL base responda desde el equipo local.
6. Existan productos con stock suficiente.

Las responsabilidades se mantienen separadas:

- `inventario-service`: administra productos y operaciones de stock.
- `sistema-pedidos-springboot`: gestiona pedidos y consume el servicio de inventario mediante HTTP.

El sistema de pedidos no debe modificar directamente `producto_inventario`; las operaciones de inventario se realizan a través de la API REST.

## 10. Pruebas y validación

Ejecutar la suite automatizada:

```bash
mvn clean test
```

O, si se dispone de Maven Wrapper:

```powershell
.\mvnw.cmd clean test
```

Para validar la API, utilizar Postman o la colección de solicitudes incluida en el repositorio. Comprobar:

- Consulta de productos.
- Consulta de producto por identificador o código.
- Consulta de stock.
- Reserva de stock por identificador.
- Reserva de stock por código.
- Devolución o liberación de stock, utilizando el endpoint y método HTTP implementados.

Los README de origen no proporcionan un conteo de pruebas automatizadas para este servicio. Incorporar el total real después de ejecutar correctamente la suite.

## 11. Material de apoyo

El repositorio documenta los siguientes recursos:

- `docs/GUIA_IMPLEMENTACION_PASO_A_PASO.md`
- `docs/INTEGRACION_CON_SISTEMA_PEDIDOS.md`
- `requests/inventario-service.http`
- `openapi/inventario-service.yaml`
- `database/02_reset_stock_demo.sql`

Estos archivos complementan las instrucciones de configuración, integración, pruebas y uso de la API.

## 12. Seguridad y consideraciones para GitHub

Las credenciales de conexión se utilizan para el entorno académico local. En producción, las contraseñas no deben almacenarse directamente en archivos versionados ni en el código fuente. Se recomienda externalizarlas mediante variables de entorno, un DataSource administrado por WildFly o un mecanismo de gestión de secretos.

No publicar:

- Contraseñas ni cadenas de conexión privadas.
- Archivos locales de configuración que contengan secretos.
- Tokens o claves de acceso.
- Logs, carpetas temporales ni artefactos generados.

Versionar el código fuente, `pom.xml`, scripts de ejecución necesarios, configuración no sensible, pruebas y documentación.

## 13. Diferencias de configuración entre los README originales

Los documentos proporcionados contienen diferencias que no deben mezclarse como si fueran una sola configuración:

- **Base de datos:** el README más reciente especifica MySQL 8.x, el usuario `inventario_app` y la tabla `producto_inventario`. El anterior mencionaba SQL Server y una instancia local de SQL Server.
- **Datasource JNDI:** el documento más reciente utiliza `java:jboss/datasources/InventarioDS`; el anterior menciona `java:/InventarioDS`. Debe utilizarse el nombre que coincida con la configuración real de WildFly y `persistence.xml`.
- **Ejecución de WildFly:** el documento más reciente indica `mvn clean wildfly:run`, mientras que el anterior recomienda revisar los scripts y el `pom.xml`. Confirmar que el plugin y el goal estén configurados antes de ejecutar el comando.
- **Maven Wrapper:** utilizar `mvnw.cmd` únicamente si está incluido en la raíz del repositorio.
- **Pruebas:** no se declara un número de pruebas exitosas porque los documentos de origen no proporcionan un resultado numérico para este servicio.

Antes de ejecutar el proyecto en otro equipo, verificar `pom.xml`, `persistence.xml`, `config/database.properties`, los scripts SQL y la configuración de WildFly. La configuración efectiva del repositorio es la referencia definitiva.

---

**Documento unificado de apoyo para la instalación, ejecución, integración y validación de `inventario-service`.**
