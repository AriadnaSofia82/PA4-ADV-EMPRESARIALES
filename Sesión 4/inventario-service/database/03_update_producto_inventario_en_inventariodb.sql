/*
  Restablece el stock de demostración sin recrear la base de datos.
  No modifica los precios.
*/

-- Seleccionar base de datos
USE InventarioDB;


-- Restablecer stock de productos
UPDATE producto_inventario
SET stock = 10
WHERE codigo = 'LAP-001';

UPDATE producto_inventario
SET stock = 20
WHERE codigo = 'MON-001';

UPDATE producto_inventario
SET stock = 30
WHERE codigo = 'TEC-001';


-- Consultar productos
SELECT
    id,
    codigo,
    nombre,
    precio,
    stock,
    version
FROM producto_inventario
ORDER BY id;