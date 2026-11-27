/*
  MIGRACIÓN para una InventarioDB ya creada con la versión anterior.
  Agrega la columna precio sin eliminar el stock actual.
*/

-- =====================================================
-- 1. SELECCIONAR BASE DE DATOS
-- =====================================================

USE InventarioDB;


-- =====================================================
-- 2. AGREGAR COLUMNA PRECIO SI NO EXISTE
-- =====================================================

SET @existe_precio = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'producto_inventario'
      AND COLUMN_NAME = 'precio'
);

SET @sql = IF(
    @existe_precio = 0,
    'ALTER TABLE producto_inventario ADD COLUMN precio DECIMAL(12,2) NULL',
    'SELECT ''La columna precio ya existe'' AS mensaje'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- =====================================================
-- 3. ACTUALIZAR PRECIOS DE LOS PRODUCTOS
-- =====================================================

UPDATE producto_inventario
SET precio = CASE codigo
    WHEN 'LAP-001' THEN 2500.00
    WHEN 'MON-001' THEN 850.00
    WHEN 'TEC-001' THEN 120.00
    ELSE COALESCE(precio, 0.00)
END;


-- =====================================================
-- 4. ESTABLECER PRECIO COMO NOT NULL
-- =====================================================

ALTER TABLE producto_inventario
MODIFY COLUMN precio DECIMAL(12,2) NOT NULL;


-- =====================================================
-- 5. AGREGAR RESTRICCIÓN CHECK SI NO EXISTE
-- =====================================================

SET @existe_check = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'producto_inventario'
      AND CONSTRAINT_NAME = 'CK_producto_inventario_precio'
      AND CONSTRAINT_TYPE = 'CHECK'
);

SET @sql = IF(
    @existe_check = 0,
    'ALTER TABLE producto_inventario ADD CONSTRAINT CK_producto_inventario_precio CHECK (precio >= 0)',
    'SELECT ''La restricción CHECK ya existe'' AS mensaje'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- =====================================================
-- 6. CONSULTAR PRODUCTOS
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