<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Revisión de pedidos - ISIL</title>
    <c:url var="cssUrl" value="/assets/css/app.css"/>
    <link rel="stylesheet" href="${cssUrl}">
    <style>
        .revision-descripcion {
            color: #64748b;
            margin-bottom: 20px;
            line-height: 1.6;
        }
        .revision-observacion {
            width: 100%;
            min-height: 85px;
            resize: vertical;
            padding: 10px;
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            font-family: inherit;
            box-sizing: border-box;
        }
        .revision-observacion:focus {
            outline: 2px solid #2563eb;
            border-color: #2563eb;
        }
        .revision-formulario {
            min-width: 220px;
        }
        .revision-formulario button {
            margin-top: 10px;
        }
        .estado-pendiente {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 20px;
            background: #fef3c7;
            color: #92400e;
            font-size: 12px;
            font-weight: bold;
        }
        .revision-vacia {
            padding: 25px;
            text-align: center;
            color: #64748b;
        }
        .revision-acciones {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            align-items: center;
        }
        .revision-id {
            font-weight: bold;
        }
        .boton-volver {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            min-height: 44px;
            padding: 11px 20px;
            background: #475569;
            color: #ffffff;
            border-radius: 9px;
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
            box-shadow: 0 3px 8px rgba(0, 0, 0, 0.12);
            transition: all 0.25s ease;
        }

        .boton-volver:hover {
            background: #334155;
            color: #ffffff;
            transform: translateY(-2px);
            box-shadow: 0 5px 12px rgba(0, 0, 0, 0.18);
        }

        .boton-volver:focus-visible {
            outline: 3px solid #93c5fd;
            outline-offset: 3px;
        }
    </style>
</head>
<body>
<div class="contenedor">
    <!-- CABECERA -->
    <header class="cabecera">
        <div>
            <p class="eyebrow">
                Spring Boot 4.1 · Spring MVC · MySQL · JDBC
            </p>
            <h1>Revisión de pedidos</h1>
            <p class="subtitulo">
                Gestión y control de revisión de los pedidos registrados.
            </p>
        </div>
        <!-- CERRAR SESIÓN -->
        <c:url var="logoutUrl" value="/logout"/>
        <form method="post" action="${logoutUrl}">
            <input
                    type="hidden"
                    name="${_csrf.parameterName}"
                    value="${_csrf.token}">
            <button type="submit">
                Cerrar sesión
            </button>
        </form>
        <div class="flujo">
            Controller → Service → JDBC Repository → MySQL
        </div>
    </header>
    <!-- MENSAJES -->
    <section class="tarjeta">
        <c:if test="${not empty mensaje}">
            <div class="alerta exito">
                <c:out value="${mensaje}"/>
            </div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alerta error">
                <c:out value="${error}"/>
            </div>
        </c:if>
        <h2>Administración de revisiones</h2>
        <p class="revision-descripcion">
            En esta sección se muestran los pedidos registrados
            que todavía no han sido revisados.

            Para completar una revisión, ingresa una observación
            y selecciona el botón correspondiente.
        </p>
        <div class="revision-acciones">
            <c:url var="pedidosUrl" value="/pedidos"/>
            <a href="${pedidosUrl}" class="boton-accion boton-volver">
                <span class="boton-icono" aria-hidden="true">←</span>
                <span>Volver a pedidos</span>
            </a>
        </div>
    </section>
    <!-- PEDIDOS PENDIENTES -->
    <section class="tarjeta">
        <div class="titulo-tabla">
            <div>
                <h2>Pedidos pendientes de revisión</h2>
                <p>
                    Total de pedidos pendientes:
                    <strong><c:out value="${pedidosPendientes.size()}"/></strong>
                </p>
                <p>
                    Cada pedido solamente puede recibir una revisión.
                </p>
            </div>
        </div>
        <div class="tabla-scroll">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Cliente</th>
                    <th>Producto</th>
                    <th>Código</th>
                    <th>Cantidad</th>
                    <th>Total</th>
                    <th>Fecha</th>
                    <th>Estado</th>
                    <th>Observación y acción</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach
                        var="pedido"
                        items="${pedidosPendientes}">
                    <tr>

                        <td class="revision-id">
                            #<c:out value="${pedido.id}"/>
                        </td>
                        <td>
                            <c:out value="${pedido.cliente}"/>
                        </td>
                        <td>
                            <c:out value="${pedido.productoNombre}"/>
                        </td>
                        <td>
                            <c:out value="${pedido.productoCodigo}"/>
                        </td>
                        <td>
                            <c:out value="${pedido.cantidad}"/>
                        </td>
                        <td>
                            S/
                            <fmt:formatNumber
                                    value="${pedido.total}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2"/>
                        </td>
                        <td>
                            <c:out value="${pedido.fecha}"/>
                        </td>
                        <td>
                            <span class="estado-pendiente">
                                Pendiente
                            </span>
                        </td>
                        <td>
                            <div class="revision-formulario">
                                <c:url
                                        var="revisionUrl"
                                        value="/revision"/>
                                <form
                                        method="post"
                                        action="${revisionUrl}">
                                    <!-- CSRF -->
                                    <input
                                            type="hidden"
                                            name="${_csrf.parameterName}"
                                            value="${_csrf.token}">
                                    <!-- ID DEL PEDIDO -->
                                    <input
                                            type="hidden"
                                            name="pedidoId"
                                            value="${pedido.id}">
                                    <!-- OBSERVACIÓN -->
                                    <textarea
                                            class="revision-observacion"
                                            name="observacion"
                                            maxlength="500"
                                            minlength="1"
                                            required
                                            oninput="this.setCustomValidity('')"
                                            oninvalid="this.setCustomValidity('Ingresa una observación válida.')"
                                            placeholder="Escribe la observación de la revisión..."></textarea>
                                    <button type="submit">
                                        Registrar revisión
                                    </button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <!-- SIN PEDIDOS PENDIENTES -->
                <c:if test="${empty pedidosPendientes}">
                    <tr>
                        <td colspan="9" class="revision-vacia">
                            No existen pedidos pendientes de revisión.
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
    <!-- INFORMACIÓN -->
    <section class="tarjeta">
        <h2>Información del proceso</h2>
        <p class="revision-descripcion">
            Al registrar una revisión, el sistema actualiza
            el indicador de revisión del pedido y almacena
            la observación junto con su fecha correspondiente
            en la tabla pedido_revision.
        </p>
        <p class="revision-descripcion">
            Las operaciones se ejecutan dentro de una transacción
            para garantizar la consistencia de los datos.
        </p>
    </section>
</div>
</body>
</html>
