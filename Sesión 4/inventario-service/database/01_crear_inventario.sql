
-- =====================================================
-- INSTALACION DE INVENTARIODB EN MYSQL
-- =====================================================

CREATE DATABASE IF NOT EXISTS InventarioDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE InventarioDB;

-- Crear usuario de inventario
CREATE USER IF NOT EXISTS 'inventario_app'@'localhost'
IDENTIFIED BY 'Inventario2026!';

-- Crear tabla de productos
CREATE TABLE IF NOT EXISTS producto_inventario (
                                                   id BIGINT NOT NULL AUTO_INCREMENT,
                                                   codigo VARCHAR(30) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uk_producto_codigo (codigo),

    CONSTRAINT chk_producto_precio CHECK (precio >= 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0)

    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci;

-- Insertar productos de demostración
INSERT INTO producto_inventario
(codigo, nombre, precio, stock, version)
VALUES
    ('LAP-001', 'Laptop', 2500.00, 10, 0),
    ('MON-001', 'Monitor', 850.00, 20, 0),
    ('TEC-001', 'Teclado', 120.00, 30, 0)
    ON DUPLICATE KEY UPDATE
                         nombre = VALUES(nombre),
                         precio = VALUES(precio);

-- Permisos del usuario
GRANT SELECT, INSERT, UPDATE, DELETE
      ON InventarioDB.producto_inventario
          TO 'inventario_app'@'localhost';

FLUSH PRIVILEGES;

-- Verificar productos
SELECT id, codigo, nombre, precio, stock, version
FROM producto_inventario
ORDER BY id;
