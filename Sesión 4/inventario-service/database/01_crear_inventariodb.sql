/*
  Instalación limpia de InventarioDB.
  Ejecutar con una cuenta administradora en MySQL Workbench.
*/

-- =====================================================
-- 1. CREAR BASE DE DATOS
-- =====================================================

CREATE DATABASE IF NOT EXISTS InventarioDB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Seleccionar base de datos
USE InventarioDB;


-- =====================================================
-- 2. CREAR USUARIO DE APLICACIÓN
-- =====================================================

CREATE USER IF NOT EXISTS 'inventario_app'@'localhost'
IDENTIFIED BY 'Inventario2026!';

-- Permisos sobre la base de datos
GRANT SELECT, INSERT, UPDATE, DELETE
ON InventarioDB.producto_inventario
TO 'inventario_app'@'localhost';


-- =====================================================
-- 3. CREAR TABLA PRODUCTO_INVENTARIO
-- =====================================================

CREATE TABLE IF NOT EXISTS producto_inventario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT PK_producto_inventario
        PRIMARY KEY (id),

    CONSTRAINT UQ_producto_inventario_codigo
        UNIQUE (codigo),

    CONSTRAINT CK_producto_inventario_precio
        CHECK (precio >= 0),

    CONSTRAINT CK_producto_inventario_stock
        CHECK (stock >= 0)

) ENGINE=InnoDB;


-- =====================================================
-- 4. INSERTAR O ACTUALIZAR PRODUCTOS
-- =====================================================

INSERT INTO producto_inventario
    (codigo, nombre, precio, stock, version)
VALUES
    ('LAP-001', 'Laptop', 2500.00, 10, 0),
    ('MON-001', 'Monitor', 850.00, 20, 0),
    ('TEC-001', 'Teclado', 120.00, 30, 0)

ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    precio = VALUES(precio);


-- =====================================================
-- 5. CONSULTAR PRODUCTOS
-- =====================================================

SELECT
    id,
    codigo,
    nombre,
    precio,
    stock,
    version
FROM producto_inventario
ORDER BY id;