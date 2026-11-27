<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reportes de pedidos</title>
    <style>
            * {
                box-sizing: border-box;
            }
            body {
                margin: 0;
                padding: 24px;
                font-family: Arial, Helvetica, sans-serif;
                background: #f4f6f9;
                color: #263238;
            }
            .contenedor {
                max-width: 1250px;
                margin: 0 auto;
            }
            .encabezado {
                display: flex;
                justify-content: space-between;
                align-items: center;
                flex-wrap: wrap;
                gap: 15px;
                margin-bottom: 25px;
            }
            h1 {
                margin: 0 0 7px;
                color: #263b53;
            }
            .subtitulo {
                margin: 0;
                color: #607080;
            }
            .acciones {
                display: flex;
                gap: 10px;
                flex-wrap: wrap;
            }
            .boton {
                display: inline-block;
                padding: 10px 16px;
                border: none;
                border-radius: 5px;
                background: #245b8f;
                color: white;
                text-decoration: none;
                cursor: pointer;
                font-size: 14px;
            }
            .boton:hover {
                background: #19436b;
            }
            .boton-secundario {
                background: #526575;
            }
            .boton-secundario:hover {
                background: #3d4d5a;
            }
            .seccion {
                background: white;
                padding: 22px;
                margin-bottom: 22px;
                border-radius: 8px;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
            }
            h2 {
                margin-top: 0;
                color: #29445f;
                font-size: 20px;
            }
            h3 {
                color: #40566b;
                font-size: 16px;
            }
            .resumen-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
                gap: 15px;
            }
            .resumen-card {
                padding: 18px;
                background: #edf3f8;
                border-left: 4px solid #3574a5;
                border-radius: 5px;
            }
            .resumen-card span {
                display: block;
                color: #536777;
                font-size: 13px;
                margin-bottom: 8px;
            }
            .resumen-card strong {
                font-size: 22px;
                color: #203e58;
            }
            .formulario {
                display: flex;
                align-items: end;
                flex-wrap: wrap;
                gap: 12px;
                margin: 15px 0;
            }
            .campo {
                display: flex;
                flex-direction: column;
                gap: 6px;
                min-width: 190px;
            }
            label {
                font-size: 14px;
                font-weight: bold;
                color: #40566b;
            }
            input {
                padding: 10px;
                border: 1px solid #cbd3dc;
                border-radius: 5px;
                font-size: 14px;
            }
            .tabla-contenedor {
                width: 100%;
                overflow-x: auto;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 12px;
                font-size: 14px;
            }
            th, td {
                padding: 11px 12px;
                border-bottom: 1px solid #e2e7ec;
                text-align: left;
                white-space: nowrap;
            }
            th {
                background: #eaf0f5;
                color: #304b64;
            }
            tbody tr:hover {
                background: #f7f9fb;
            }
            .mensaje {
                padding: 12px 15px;
                margin: 12px 0;
                border-radius: 5px;
                font-size: 14px;
            }
            .error {
                background: #fdecec;
                color: #a12626;
                border: 1px solid #f3c5c5;
            }
            .advertencia {
                background: #fff5df;
                color: #805b14;
                border: 1px solid #f1dfb5;
            }
            .exito {
                background: #e9f6ed;
                color: #236b39;
                border: 1px solid #c5e5ce;
            }
            .estado {
                display: inline-block;
                padding: 5px 9px;
                border-radius: 12px;
                font-size: 12px;
                font-weight: bold;
            }
            .estado-registrado {
                background: #e4f0ff;
                color: #245b8f;
            }
            .estado-anulado {
                background: #fde8e8;
                color: #a12626;
            }
            .estado-revisado {
                background: #e4f4e8;
                color: #236b39;
            }
            .estado-pendiente {
                background: #fff1d6;
                color: #805b14;
            }
            .vacio {
                padding: 15px;
                color: #687783;
                background: #f7f9fb;
                border-radius: 5px;
            }
            .pie {
                text-align: center;
                color: #71808c;
                font-size: 12px;
                margin-top: 25px;
            }
            @media (max-width: 650px) {
                body {
                    padding: 12px;
                }

                .seccion {
                    padding: 15px;
                }

                .formulario {
                    align-items: stretch;
                }

                .campo {
                    width: 100%;
                }
            }
            /* CONTENEDOR DE GRÁFICOS */
            .graficos-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
                gap: 20px;
                margin-top: 22px;
            }

            .grafico-card {
                background: #ffffff;
                border: 1px solid #e2e8f0;
                border-radius: 10px;
                padding: 20px;
                min-width: 0;
            }

            .grafico-card h3 {
                margin-top: 0;
                margin-bottom: 18px;
                color: #29445f;
                font-size: 16px;
            }

            .grafico-contenedor {
                position: relative;
                width: 100%;
                height: 320px;
            }

            .grafico-contenedor canvas {
                max-width: 100%;
            }

            @media (max-width: 600px) {
                .grafico-contenedor {
                    height: 280px;
                }
            }
        </style>
</head>
<body>
<div class="contenedor">
    <!-- ENCABEZADO -->
    <header class="cabecera">
        <div>
            <h1>Reportes de pedidos</h1>
            <p class="subtitulo">
                Consulta de estadísticas, ventas y seguimiento de pedidos.
            </p>
        </div>
        <div class="acciones">
            <a href="${pageContext.request.contextPath}/pedidos"
               class="boton boton-secundario">
                Volver a pedidos
            </a>
            <a href="${pageContext.request.contextPath}/reportes/pedidos"
               class="boton">
                Actualizar reportes
            </a>
        </div>
    </header>
    <!-- ERRORES DEL SERVICIO DE REPORTES -->
    <c:if test="${not empty errorReporte}">
        <div class="alerta error">
            <strong>Error en el servicio de reportes:</strong>
            <c:out value="${errorReporte}"/>
        </div>
    </c:if>
    <!-- ERRORES DE REVISIONES PA04 -->
    <c:if test="${not empty errorRevision}">
        <div class="alerta error">
            <strong>Error al consultar las revisiones:</strong>
            <c:out value="${errorRevision}"/>
        </div>
    </c:if>

    <c:if test="${not empty errorEstadisticasRevision}">
        <div class="alerta error">
            <strong>Error al consultar las estadísticas de revisión:</strong>
            <c:out value="${errorEstadisticasRevision}"/>
        </div>
    </c:if>
    <!-- 1. RESUMEN GENERAL -->
    <section class="tarjeta">
        <h2>1. Resumen general de pedidos</h2>
        <c:choose>
            <c:when test="${not empty resumen}">
                <div class="resumen-grid">

                    <div class="resumen-card">
                        <span>Total de pedidos</span>
                        <strong>
                            <c:out value="${resumen.totalPedidos}"/>
                        </strong>
                    </div>
                    <div class="resumen-card">
                        <span>Total de unidades vendidas</span>
                        <strong>
                            <c:out value="${resumen.totalUnidades}"/>
                        </strong>
                    </div>
                    <div class="resumen-card">
                        <span>Importe acumulado</span>
                        <strong>
                            S/
                            <fmt:formatNumber
                                value="${resumen.importeAcumulado}"
                                minFractionDigits="2"
                                maxFractionDigits="2"/>
                        </strong>
                    </div>
                    <div class="resumen-card">
                        <span>Ticket promedio</span>
                        <strong>
                            S/
                            <fmt:formatNumber
                                value="${resumen.ticketPromedio}"
                                minFractionDigits="2"
                                maxFractionDigits="2"/>
                        </strong>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <p class="sin-datos">
                    No hay información de resumen disponible.
                </p>
            </c:otherwise>
        </c:choose>
    </section>
    <!-- 2. PRODUCTOS MÁS VENDIDOS -->
    <section class="tarjeta">
        <h2>2. Productos más vendidos</h2>
        <c:choose>
            <c:when test="${not empty productos}">
                <div class="tabla-scroll">
                    <table>
                        <thead>
                        <tr>
                            <th>Código</th>
                            <th>Producto</th>
                            <th>Total de pedidos</th>
                            <th>Unidades vendidas</th>
                            <th>Importe acumulado</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="producto" items="${productos}">
                            <tr>
                                <td>
                                    <c:out value="${producto.productoCodigo}"/>
                                </td>
                                <td>
                                    <c:out value="${producto.productoNombre}"/>
                                </td>
                                <td>
                                    <c:out value="${producto.totalPedidos}"/>
                                </td>
                                <td>
                                    <c:out value="${producto.totalUnidades}"/>
                                </td>
                                <td>
                                    S/
                                    <fmt:formatNumber
                                        value="${producto.importeAcumulado}"
                                        minFractionDigits="2"
                                        maxFractionDigits="2"/>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <p class="sin-datos">
                    No hay productos vendidos para mostrar.
                </p>
            </c:otherwise>
        </c:choose>
    </section>
        <!-- 2.1 GRÁFICO DE PRODUCTOS MÁS VENDIDOS -->
        <c:if test="${not empty productos}">
            <section class="tarjeta">
                <h2>Gráfico de productos más vendidos</h2>
                <p>
                    Comparación de las unidades vendidas por producto.
                </p>
                <div class="grafico-card">
                    <div class="grafico-contenedor">
                        <canvas id="graficoProductos"></canvas>
                    </div>
                </div>
                <!-- Datos obtenidos de la lista de productos -->
                <ul id="datosProductosGrafico" hidden>
                    <c:forEach var="producto" items="${productos}">
                        <li
                            data-label="${fn:escapeXml(producto.productoNombre)}"
                            data-value="${producto.totalUnidades}">
                        </li>
                    </c:forEach>
                </ul>
            </section>
        </c:if>
     </section>
    <!-- 3. BÚSQUEDA SIMPLE -->
    <section class="tarjeta">
        <h2>3. Búsqueda de pedidos por cliente</h2>
        <p>
            Consulta los pedidos registrados utilizando el nombre del cliente.
        </p>
        <form method="get"
              action="${pageContext.request.contextPath}/reportes/pedidos"
              class="form-grid">
            <div class="campo">
                <label for="cliente">Nombre del cliente</label>
                <input type="text"
                       id="cliente"
                       name="cliente"
                       value="${fn:escapeXml(cliente)}"
                       placeholder="Ingrese el nombre del cliente"/>
            </div>
            <button type="submit" class="boton">
                Buscar pedidos
            </button>
        </form>
        <c:if test="${busquedaSimpleRealizada}">
            <h3>Resultados de búsqueda</h3>
            <c:choose>
                <c:when test="${not empty pedidos}">
                    <div class="tabla-scroll">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Cliente</th>
                                <th>Producto</th>
                                <th>Cantidad</th>
                                <th>Total</th>
                                <th>Fecha</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="pedido" items="${pedidos}">
                                <tr>
                                    <td><c:out value="${pedido.id}"/></td>
                                    <td><c:out value="${pedido.cliente}"/></td>
                                    <td><c:out value="${pedido.productoNombre}"/></td>
                                    <td><c:out value="${pedido.cantidad}"/></td>
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
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="sin-datos">
                        No se encontraron pedidos para el cliente indicado.
                    </p>
                </c:otherwise>
            </c:choose>
        </c:if>
    </section>
    <!-- 4. BÚSQUEDA AVANZADA -->
    <section class="tarjeta">
        <h2>4. Búsqueda avanzada de pedidos</h2>
        <p>
            Filtra los pedidos por cliente y por importe mínimo.
        </p>
        <form method="get"
              action="${pageContext.request.contextPath}/reportes/pedidos"
              class="form-grid">
            <div class="campo">
                <label for="clienteAvanzado">Nombre del cliente</label>
                <input type="text"
                       id="clienteAvanzado"
                       name="cliente"
                       value="${fn:escapeXml(clienteAvanzado)}"
                       placeholder="Ingrese el nombre del cliente"/>
            </div>
            <div class="campo">
                <label for="totalMinimo">Importe mínimo (S/)</label>
                <input type="number"
                       id="totalMinimo"
                       name="totalMinimo"
                       min="0"
                       step="0.01"
                       value="${totalMinimo}"
                       placeholder="Ej. 100.00"/>
            </div>
            <button type="submit" class="boton">
                Aplicar filtros
            </button>
        </form>
        <c:if test="${not empty errorBusqueda}">
            <div class="alerta error">
                <c:out value="${errorBusqueda}"/>
            </div>
        </c:if>
        <c:if test="${busquedaAvanzadaRealizada}">
            <h3>Resultados de búsqueda avanzada</h3>
            <c:choose>
                <c:when test="${not empty pedidosAvanzados}">
                    <div class="tabla-scroll">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Cliente</th>
                                <th>Producto</th>
                                <th>Precio unitario</th>
                                <th>Cantidad</th>
                                <th>Total</th>
                                <th>Fecha</th>
                                <th>Estado</th>
                                <th>Revisión PA4</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="pedido" items="${pedidosAvanzados}">
                                <tr>
                                    <td><c:out value="${pedido.id}"/></td>
                                    <td><c:out value="${pedido.cliente}"/></td>
                                    <td><c:out value="${pedido.productoNombre}"/></td>
                                    <td>
                                        S/
                                        <fmt:formatNumber
                                            value="${pedido.precioUnitario}"
                                            minFractionDigits="2"
                                            maxFractionDigits="2"/>
                                    </td>
                                    <td><c:out value="${pedido.cantidad}"/></td>
                                    <td>
                                        S/
                                        <fmt:formatNumber
                                            value="${pedido.total}"
                                            minFractionDigits="2"
                                            maxFractionDigits="2"/>
                                    </td>
                                    <td><c:out value="${pedido.fecha}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${pedido.estado == 'ANULADO'}">
                                                <span class="estado estado-anulado">
                                                    ANULADO
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="estado estado-registrado">
                                                    REGISTRADO
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${pedido.revisado}">
                                                <span class="estado estado-revisado">
                                                    REVISADO
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="estado estado-pendiente">
                                                    PENDIENTE
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>

                <c:otherwise>
                    <p class="sin-datos">
                        No se encontraron pedidos que coincidan con los filtros.
                    </p>
                </c:otherwise>
            </c:choose>
        </c:if>
    </section>
    <!-- 5. CLIENTES FRECUENTES -->
    <section class="tarjeta">
        <h2>5. Reporte de clientes frecuentes</h2>
        <p>
            Consulta los clientes que alcanzan una cantidad mínima de pedidos.
        </p>
        <form method="get"
              action="${pageContext.request.contextPath}/reportes/pedidos"
              class="form-grid">

            <div class="campo">
                <label for="minPedidos">Cantidad mínima de pedidos</label>
                <input type="number"
                       id="minPedidos"
                       name="minPedidos"
                       min="1"
                       step="1"
                       value="${minPedidos}"
                       placeholder="Ej. 2"/>
            </div>
            <button type="submit" class="boton">
                Consultar clientes
            </button>
        </form>
        <c:if test="${not empty errorFrecuentes}">
            <div class="alerta error">
                <c:out value="${errorFrecuentes}"/>
            </div>
        </c:if>
        <c:if test="${frecuentesRealizado}">
            <h3>Clientes frecuentes encontrados</h3>
            <c:choose>
                <c:when test="${not empty clientesFrecuentes}">
                    <div class="tabla-scroll">
                        <table>
                            <thead>
                            <tr>
                                <th>Cliente</th>
                                <th>Total de pedidos</th>
                                <th>Total de unidades</th>
                                <th>Importe acumulado</th>
                                <th>Ticket promedio</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="clienteFrecuente"
                                       items="${clientesFrecuentes}">
                                <tr>
                                    <td>
                                        <c:out value="${clienteFrecuente.cliente}"/>
                                    </td>
                                    <td>
                                        <c:out value="${clienteFrecuente.totalPedidos}"/>
                                    </td>
                                    <td>
                                        <c:out value="${clienteFrecuente.totalUnidades}"/>
                                    </td>
                                    <td>
                                        S/
                                        <fmt:formatNumber
                                            value="${clienteFrecuente.importeAcumulado}"
                                            minFractionDigits="2"
                                            maxFractionDigits="2"/>
                                    </td>
                                    <td>
                                        S/
                                        <fmt:formatNumber
                                            value="${clienteFrecuente.ticketPromedio}"
                                            minFractionDigits="2"
                                            maxFractionDigits="2"/>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="sin-datos">
                        No se encontraron clientes que cumplan el mínimo indicado.
                    </p>
                </c:otherwise>
            </c:choose>
        </c:if>
    </section>
    <!-- 6. ESTADÍSTICAS DE REVISIONES -->
    <section class="tarjeta">
        <h2>6. Estadísticas de revisión de pedidos</h2>

        <c:choose>
            <c:when test="${not empty estadisticasRevisiones}">
                <div class="resumen-grid">

                    <div class="resumen-card">
                        <span>Total de pedidos</span>
                        <strong>
                            <c:out value="${estadisticasRevisiones.totalPedidos}"/>
                        </strong>
                    </div>

                    <div class="resumen-card">
                        <span>Pedidos revisados</span>
                        <strong>
                            <c:out value="${estadisticasRevisiones.totalRevisados}"/>
                        </strong>
                    </div>

                    <div class="resumen-card">
                        <span>Pedidos pendientes</span>
                        <strong>
                            <c:out value="${estadisticasRevisiones.totalPendientes}"/>
                        </strong>
                    </div>

                    <div class="resumen-card">
                        <span>Porcentaje revisado</span>
                        <strong>
                            <fmt:formatNumber
                                value="${estadisticasRevisiones.porcentajeRevisado}"
                                minFractionDigits="2"
                                maxFractionDigits="2"/>%
                        </strong>
                    </div>

                </div>
                 <!-- GRÁFICO DE ESTADO DE REVISIONES -->
                 <div class="grafico-card" style="margin-top: 22px;">

                     <h3>Distribución del estado de revisión</h3>

                     <div class="grafico-contenedor">
                         <canvas id="graficoRevisiones"></canvas>
                     </div>

                     <!-- Datos estadísticos -->
                     <div id="datosRevisionesGrafico"
                          data-revisados="${estadisticasRevisiones.totalRevisados}"
                          data-pendientes="${estadisticasRevisiones.totalPendientes}"
                          hidden>
                     </div>

                 </div>
            </c:when>
            <c:otherwise>
                <p class="sin-datos">
                    No hay estadísticas de revisión disponibles.
                </p>
            </c:otherwise>
        </c:choose>
    </section>
    <!-- 7. TRAZABILIDAD DE REVISIONES -->
    <section class="tarjeta">

        <h2>7. Seguimiento y trazabilidad de pedidos</h2>

        <p>
            Consulta los pedidos según su estado de revisión y verifica
            las observaciones y fechas registradas.
        </p>

        <form method="get"
              action="${pageContext.request.contextPath}/reportes/pedidos"
              class="form-grid">

            <div class="campo">
                <label for="estadoRevision">
                    Estado de revisión
                </label>

                <select id="estadoRevision" name="estadoRevision" required>
                    <option value=""
                        <c:if test="${empty estadoRevision}">
                            selected
                        </c:if>>
                        Seleccione un estado
                    </option>

                    <option value="PENDIENTE"
                        <c:if test="${estadoRevision == 'PENDIENTE'}">
                            selected
                        </c:if>>
                        Pendiente
                    </option>

                    <option value="REVISADO"
                        <c:if test="${estadoRevision == 'REVISADO'}">
                            selected
                        </c:if>>
                        Revisado
                    </option>
                </select>
            </div>

            <button type="submit" class="boton">
                Consultar revisiones
            </button>

        </form>

        <c:if test="${busquedaRevisionRealizada}">

            <h3>
                Resultados:
                <c:out value="${estadoRevision}"/>
            </h3>

            <c:choose>

                <c:when test="${not empty revisiones}">

                    <div class="tabla-scroll">
                        <table>
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Cliente</th>
                                <th>Producto</th>
                                <th>Cantidad</th>
                                <th>Total</th>
                                <th>Estado comercial</th>
                                <th>Estado de revisión</th>
                                <th>Observación</th>
                                <th>Fecha de revisión</th>
                            </tr>
                            </thead>

                            <tbody>

                            <c:forEach var="revision" items="${revisiones}">
                                <tr>

                                    <td>
                                        <c:out value="${revision.id}"/>
                                    </td>

                                    <td>
                                        <c:out value="${revision.cliente}"/>
                                    </td>

                                    <td>
                                        <c:out value="${revision.productoNombre}"/>
                                    </td>

                                    <td>
                                        <c:out value="${revision.cantidad}"/>
                                    </td>

                                    <td>
                                        S/
                                        <fmt:formatNumber
                                            value="${revision.total}"
                                            minFractionDigits="2"
                                            maxFractionDigits="2"/>
                                    </td>

                                    <td>
                                        <c:out value="${revision.estado}"/>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${revision.revisado}">
                                                <span class="estado estado-revisado">
                                                    REVISADO
                                                </span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="estado estado-pendiente">
                                                    PENDIENTE
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty revision.observacion}">
                                                <c:out value="${revision.observacion}"/>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="sin-datos">
                                                    Sin observaciones
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty revision.fechaRevision}">
                                                <c:out value="${revision.fechaRevision}"/>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="sin-datos">
                                                    Sin fecha de revisión
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                </tr>
                            </c:forEach>

                            </tbody>
                        </table>
                    </div>

                </c:when>

                <c:otherwise>
                    <p class="sin-datos">
                        No se encontraron pedidos para el estado de revisión seleccionado.
                    </p>
                </c:otherwise>

            </c:choose>

        </c:if>

    </section>
    <!-- NAVEGACIÓN Y CIERRE DE SESIÓN -->
    <footer class="pie">
        <p>Sistema de gestión de pedidos</p>
        <form method="post"
              action="${pageContext.request.contextPath}/logout">
            <input type="hidden"
                   name="${_csrf.parameterName}"
                   value="${_csrf.token}"/>

            <button type="submit" class="boton boton-secundario">
                Cerrar sesión
            </button>
        </form>
    </footer>
</div>
<!-- BIBLIOTECA CHART.JS -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<!-- CONFIGURACIÓN DE GRÁFICOS -->
<script>
document.addEventListener("DOMContentLoaded", function () {
    /* =========================================
       1. GRÁFICO DE PRODUCTOS MÁS VENDIDOS
       ========================================= */
    const datosProductos = document.querySelectorAll(
        "#datosProductosGrafico li"
    );
    const canvasProductos = document.getElementById(
        "graficoProductos"
    );
    if (canvasProductos && datosProductos.length > 0) {
        const nombresProductos = [];
        const unidadesVendidas = [];
        datosProductos.forEach(function (item) {
            nombresProductos.push(item.dataset.label);
            unidadesVendidas.push(Number(item.dataset.value));
        });
        new Chart(canvasProductos, {
            type: "bar",
            data: {
                labels: nombresProductos,

                datasets: [{
                    label: "Unidades vendidas",
                    data: unidadesVendidas,
                    backgroundColor: "rgba(37, 99, 235, 0.75)",
                    borderColor: "#2563eb",
                    borderWidth: 1,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                indexAxis: "y",
                plugins: {
                    legend: {
                        display: true,
                        position: "bottom"
                    },

                    tooltip: {
                        enabled: true
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        },
                        title: {
                            display: true,
                            text: "Unidades vendidas"
                        }
                    },
                    y: {
                        title: {
                            display: true,
                            text: "Productos"
                        }
                    }
                }
            }
        });
    }
    /* =========================================
       2. GRÁFICO DE ESTADO DE REVISIONES
       ========================================= */
    const datosRevisiones = document.getElementById(
        "datosRevisionesGrafico"
    );
    const canvasRevisiones = document.getElementById(
        "graficoRevisiones"
    );
    if (datosRevisiones && canvasRevisiones) {
        const revisados = Number(
            datosRevisiones.dataset.revisados
        );
        const pendientes = Number(
            datosRevisiones.dataset.pendientes
        );
        if (revisados + pendientes > 0) {
            new Chart(canvasRevisiones, {
                type: "doughnut",
                data: {
                    labels: [
                        "Pedidos revisados",
                        "Pedidos pendientes"
                    ],
                    datasets: [{
                        data: [
                            revisados,
                            pendientes
                        ],
                        backgroundColor: [
                            "#16a34a",
                            "#f59e0b"
                        ],
                        borderColor: [
                            "#ffffff",
                            "#ffffff"
                        ],
                        borderWidth: 3
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: "bottom"
                        },
                        tooltip: {
                            callbacks: {
                                label: function (context) {
                                    return context.label + ": "
                                        + context.raw + " pedidos";
                                }
                            }
                        }
                    }
                }
            });
        } else {
            canvasRevisiones.parentElement.innerHTML =
                "<p class='sin-datos'>"
                + "No hay pedidos registrados para generar el gráfico."
                + "</p>";
        }
    }
});
</script>
</body>
</html>
