/*
  Crea la tabla pedido si todavía no existe.
  No elimina ni modifica los datos existentes.
*/

-- Seleccionar base de datos
USE InventarioDB;


-- =====================================================
-- CREAR TABLA PEDIDO
-- =====================================================

CREATE TABLE IF NOT EXISTS pedido (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente VARCHAR(120) NOT NULL,
    producto_codigo VARCHAR(30) NOT NULL,
    producto_nombre VARCHAR(120) NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    cantidad INT NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    fecha DATETIME NOT NULL,

    CONSTRAINT PK_pedido
        PRIMARY KEY (id)

) ENGINE=InnoDB;