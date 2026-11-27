
-- =====================================================
-- RESTABLECER STOCK DE DEMOSTRACION
-- MYSQL
-- =====================================================

USE InventarioDB;

UPDATE producto_inventario
SET stock = 10
WHERE codigo = 'LAP-001';

UPDATE producto_inventario
SET stock = 20
WHERE codigo = 'MON-001';

UPDATE producto_inventario
SET stock = 30
WHERE codigo = 'TEC-001';

-- Verificar resultados
SELECT id, codigo, nombre, precio, stock, version
FROM producto_inventario
ORDER BY id;
