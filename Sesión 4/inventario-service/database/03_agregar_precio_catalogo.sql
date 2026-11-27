
-- =====================================================
-- MIGRACION: AGREGAR PRECIO A PRODUCTO_INVENTARIO
-- MYSQL
-- =====================================================

USE InventarioDB;

-- Agregar precio si todavía no existe
SET @existe_precio = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'producto_inventario'
      AND COLUMN_NAME = 'precio'
);

SET @sql_precio = IF(
    @existe_precio = 0,
    'ALTER TABLE producto_inventario
     ADD COLUMN precio DECIMAL(12,2) NULL',
    'SELECT "La columna precio ya existe" AS mensaje'
);

PREPARE stmt_precio FROM @sql_precio;
EXECUTE stmt_precio;
DEALLOCATE PREPARE stmt_precio;

-- Actualizar precios sin modificar el stock
UPDATE producto_inventario
SET precio = CASE codigo
                 WHEN 'LAP-001' THEN 2500.00
                 WHEN 'MON-001' THEN 850.00
                 WHEN 'TEC-001' THEN 120.00
                 ELSE COALESCE(precio, 0.00)
    END
WHERE precio IS NULL
   OR codigo IN ('LAP-001', 'MON-001', 'TEC-001');

-- Comprobar que no queden precios nulos
ALTER TABLE producto_inventario
    MODIFY COLUMN precio DECIMAL(12,2) NOT NULL;

-- Verificar resultados
SELECT id, codigo, nombre, precio, stock, version
FROM producto_inventario
ORDER BY id;
