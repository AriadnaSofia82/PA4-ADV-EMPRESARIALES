# Sistema de Pedidos – PA4

Solución empresarial distribuida para la gestión de pedidos, la integración con inventario y la supervisión del proceso de revisión. El proyecto está compuesto por tres aplicaciones independientes que se comunican mediante servicios REST y comparten responsabilidades claramente delimitadas.

## 1. Descripción general

La solución permite gestionar pedidos, consultar y reservar stock, registrar la revisión de pedidos y consultar información de seguimiento y estadísticas.

La arquitectura está organizada en tres proyectos:

| Proyecto | Tecnología principal | Puerto local | Responsabilidad |
|---|---|---:|---|
| `sistema-pedidos-springboot` | Spring Boot | 8081 | Aplicación principal: gestión de pedidos y registro transaccional de revisiones. |
| `pedidos-reportes-service` | Spring Boot + Spring JDBC | 8083 | API de consulta para pedidos pendientes, revisados y estadísticas. |
| `inventario-service` | Jakarta EE 11 + WildFly | 8082 | API de inventario: productos, consulta de stock, reservas y operaciones de devolución/liberación implementadas. |

## 2. Arquitectura e integración

```text
                    ┌──────────────────────────────┐
                    │ sistema-pedidos-springboot   │
                    │ Spring Boot · Puerto 8081    │
                    │                              │
                    │ Gestión de pedidos           │
                    │ Revisión transaccional       │
                    └──────────────┬───────────────┘
                                   │
                 ┌─────────────────┴─────────────────┐
                 │                                   │
          REST / HTTP                         REST / HTTP
                 │                                   │
                 ▼                                   ▼
┌────────────────────────────┐       ┌────────────────────────────┐
│ inventario-service         │       │ pedidos-reportes-service   │
│ Jakarta EE 11 / WildFly    │       │ Spring Boot / Spring JDBC  │
│ Puerto 8082                │       │ Puerto 8083                │
│                            │       │                            │
│ Productos y stock          │       │ Consultas de revisión      │
│ Reservas y devoluciones    │       │ Estadísticas                │
└──────────────┬─────────────┘       └──────────────┬─────────────┘
               │                                    │
               ▼                                    ▼
       SQL Server                              MySQL
       InventarioDB                            InventarioDB
                                         (pedidos y reportes)
```

**Importante sobre las bases de datos:** en el entorno de desarrollo utilizado, `inventario-service` se conecta a SQL Server mediante el datasource JNDI `InventarioDS`, mientras que las aplicaciones Spring utilizan MySQL para los datos de pedidos y reportes. Aunque ambas bases puedan tener el nombre `InventarioDB`, corresponden a motores y conexiones independientes.

### Responsabilidades por componente

- **Aplicación principal:** concentra el flujo de pedidos y registra las revisiones. La revisión actualiza el indicador del pedido e inserta su registro de trazabilidad dentro de una operación transaccional.
- **Servicio de inventario:** ofrece las operaciones de consulta y gestión de stock que requiere el flujo de pedidos.
- **Servicio de reportes:** consulta la información y expone resultados para supervisar el estado de las revisiones. Es un servicio de lectura; el registro de revisiones corresponde a la aplicación principal.

## 3. Tecnologías utilizadas

- Java 21.
- Spring Boot 4.1.1 en los proyectos Spring (según la configuración documentada).
- Spring Web, Spring MVC, Spring Data JPA y Spring JDBC, según el módulo.
- Jakarta EE 11, JAX-RS, JPA/Hibernate y WildFly 41.0.0.Final para inventario.
- MySQL 8.x para pedidos y reportes.
- SQL Server para inventario.
- Maven Wrapper.
- JUnit 5 y Mockito para pruebas automatizadas de los proyectos Spring.

Las versiones efectivas deben contrastarse con los `pom.xml` de cada módulo antes de actualizar esta documentación.

## 4. Requisitos previos

- JDK 21 configurado en `JAVA_HOME`.
- MySQL 8.x disponible y con la base de datos `InventarioDB` preparada para pedidos y reportes.
- SQL Server disponible y con la base de datos `InventarioDB` preparada para inventario.
- Datasource JNDI `InventarioDS` correctamente configurado para `inventario-service`.
- Puertos locales `8081`, `8082` y `8083` disponibles.
- PowerShell o una terminal compatible.
- Maven Wrapper incluido en cada proyecto Spring; para inventario, utilizar el wrapper si está incluido.

Cada integrante debe configurar sus propias credenciales y cadenas de conexión. No se deben subir contraseñas, tokens ni archivos `.env` con secretos al repositorio.

## 5. Configuración

### 5.1. Aplicaciones Spring Boot

Configurar en cada proyecto Spring el datasource MySQL y las propiedades externas que realmente estén referenciadas en su `application.properties` o `application.yml`.

Ejemplo de URL local documentada:

```properties
jdbc:mysql://localhost:3306/InventarioDB?serverTimezone=America/Lima
```

En el entorno documentado se contemplaron variables de entorno como:

```properties
DB_URL=jdbc:mysql://localhost:3306/InventarioDB?serverTimezone=America/Lima
DB_USERNAME=<usuario_mysql>
DB_PASSWORD=<contraseña_mysql>
```

Los nombres definitivos deben coincidir con los placeholders de la configuración vigente de cada aplicación.

La aplicación principal también debe apuntar a los servicios REST:

```properties
# Servicio de inventario
http://localhost:8082/inventario-service/api/inventario

# Servicio de reportes
http://localhost:8083/pedidos-reportes-service/api/v1/pedidos
```

### 5.2. Servicio de inventario

Configurar el datasource JNDI `InventarioDS` para que apunte a la instancia SQL Server local y a la base de datos de inventario. La instancia utilizada durante el desarrollo fue específica del equipo; cada entorno debe establecer su propia conexión.

El despliegue local documentado utilizó WildFly 41 con un desplazamiento de puertos de `2`, por lo que el servicio quedó disponible en el puerto `8082`. Utilizar los scripts y el mecanismo de ejecución incluidos en el proyecto; no asumir un comando de arranque que no esté definido en su `pom.xml` o documentación.

## 6. Ejecución del sistema completo

Se recomienda iniciar los componentes en el siguiente orden:

1. Iniciar SQL Server y comprobar la base de datos de inventario.
2. Verificar el datasource `InventarioDS`.
3. Iniciar `inventario-service` y comprobar que responda en el puerto `8082`.
4. Iniciar MySQL y comprobar la base de datos utilizada por pedidos y reportes.
5. Iniciar `pedidos-reportes-service` en el puerto `8083`.
6. Iniciar `sistema-pedidos-springboot` en el puerto `8081`.

### Comandos Maven de referencia

En cada proyecto Spring, desde PowerShell:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Para generar el artefacto de inventario, si el wrapper está disponible:

```powershell
.\mvnw.cmd clean package
```

El arranque de WildFly y el despliegue del WAR deben realizarse con el procedimiento configurado en `inventario-service`.

## 7. URLs y endpoints principales

### 7.1. Aplicación principal

URL base local:

```text
http://localhost:8081/sistema-pedidos
```

Registro de revisión:

```http
POST http://localhost:8081/sistema-pedidos/revision
```

El contrato exacto de solicitud y respuesta debe consultarse en el DTO y controlador vigentes del proyecto.

### 7.2. Servicio de inventario

URL base:

```text
http://localhost:8082/inventario-service/api/inventario
```

| Método | Recurso | Función |
|---|---|---|
| GET | `/productos` | Consultar productos. |
| GET | `/productos/{id}/stock` | Consultar stock por identificador. |
| POST | `/productos/{id}/reservas` | Registrar una reserva por identificador. |
| GET | `/productos/codigo/{codigo}/stock` | Consultar stock por código. |
| POST | `/productos/codigo/{codigo}/reservas` | Registrar una reserva por código. |

El servicio también contempla operaciones de devolución o liberación de stock. Para conocer la ruta y el verbo HTTP exactos, revisar el recurso JAX-RS implementado; no se debe asumir un endpoint distinto al definido en el código.

### 7.3. Servicio de reportes

URL base:

```text
http://localhost:8083/pedidos-reportes-service/api/v1/pedidos
```

| Método | Endpoint | Función |
|---|---|---|
| GET | `/revisiones?estado=PENDIENTE` | Consultar pedidos pendientes de revisión. |
| GET | `/revisiones?estado=REVISADO` | Consultar pedidos revisados. |
| GET | `/estadisticas/revisiones` | Consultar estadísticas del proceso de revisión. |

Las respuestas se entregan en JSON. Los atributos exactos dependen de los DTO y proyecciones vigentes.

## 8. Flujo de revisión de pedidos

El proceso de revisión se registra en `sistema-pedidos-springboot`. A nivel funcional, el flujo contempla:

1. Recibir la solicitud de revisión.
2. Verificar que el pedido exista.
3. Validar la observación y comprobar que el pedido no haya sido revisado previamente.
4. Actualizar el indicador de revisión del pedido.
5. Insertar el registro correspondiente en `pedido_revision`.
6. Confirmar la operación y permitir que el servicio de reportes refleje el nuevo estado.

La actualización del pedido y la inserción de la trazabilidad se ejecutan como una operación transaccional en la implementación documentada. La restricción única sobre `pedido_revision.pedido_id` refuerza la prevención de registros duplicados.

El servicio de reportes no realiza estas modificaciones: consulta los datos para mostrar pendientes, revisados y estadísticas.

## 9. Pruebas y validación

### 9.1. Pruebas automatizadas

Ejecutar las pruebas de cada proyecto Spring:

```powershell
.\mvnw.cmd clean test
```

La ejecución documentada en el informe PA4 registró:

| Proyecto | Pruebas ejecutadas | Exitosas | Fallidas | Omitidas |
|---|---:|---:|---:|---:|
| `sistema-pedidos-springboot` | 56 | 56 | 0 | 0 |
| `pedidos-reportes-service` | 31 | 31 | 0 | 0 |

Estos resultados corresponden a la ejecución documentada y pueden variar si se modifica el código. Para `inventario-service`, ejecutar y registrar el resultado real de la suite disponible; no se consigna aquí un total que no haya sido verificado.

### 9.2. Validación mediante Postman

Para validar la integración de extremo a extremo:

1. Comprobar que los tres servicios estén levantados.
2. Consultar productos y stock en `inventario-service`.
3. Ejecutar los escenarios de pedidos que interactúan con el inventario.
4. Consultar los pedidos pendientes en `pedidos-reportes-service`.
5. Registrar la revisión desde la aplicación principal.
6. Volver a consultar pendientes y comprobar el cambio de estado.
7. Consultar pedidos revisados y estadísticas.
8. Probar escenarios controlados, como pedido inexistente, observación inválida o revisión duplicada, según los contratos implementados.

Los cuerpos JSON, códigos HTTP y respuestas esperadas deben tomarse de los DTO, controladores y recursos REST actuales.

## 10. Estructura del repositorio

El repositorio contiene tres proyectos independientes:

```text
PA4/
├── sistema-pedidos-springboot/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── pedidos-reportes-service/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── inventario-service/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
└── README.md
```

La estructura interna de cada proyecto conserva sus propias capas, paquetes, recursos, configuración y pruebas.

## 11. Seguridad y buenas prácticas

- No subir credenciales, contraseñas, tokens ni cadenas de conexión privadas.
- Mantener la configuración sensible fuera del control de versiones.
- No versionar carpetas `target/`, logs ni archivos temporales del IDE.
- Mantener Maven Wrapper y archivos de configuración no sensibles necesarios para reproducir el proyecto.
- Actualizar este README cuando cambien puertos, rutas, dependencias, variables de entorno o responsabilidades.
- Verificar los endpoints y contratos directamente en el código antes de documentar cambios.

## 12. Estado de la documentación

Este README describe la arquitectura general y las configuraciones locales documentadas para PA4. Para detalles específicos de instalación, configuración, ejecución y pruebas de cada componente, revisar el README ubicado en la carpeta correspondiente:

- `sistema-pedidos-springboot/README.md`
- `pedidos-reportes-service/README.md`
- `inventario-service/README.md`
