
USE InventarioDB;

-- =====================================================
-- 1. AGREGAR COLUMNA estado A pedido
-- =====================================================

SET @existe_estado = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pedido'
      AND COLUMN_NAME = 'estado'
);

SET @sql_estado = IF(
    @existe_estado = 0,
    'ALTER TABLE pedido
     ADD COLUMN estado VARCHAR(30) NOT NULL DEFAULT ''REGISTRADO''',
    'SELECT "La columna estado ya existe" AS mensaje'
);

PREPARE stmt_estado FROM @sql_estado;
EXECUTE stmt_estado;
DEALLOCATE PREPARE stmt_estado;


-- =====================================================
-- 2. AGREGAR COLUMNA revisado A pedido
-- =====================================================

SET @existe_revisado = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pedido'
      AND COLUMN_NAME = 'revisado'
);

SET @sql_revisado = IF(
    @existe_revisado = 0,
    'ALTER TABLE pedido
     ADD COLUMN revisado BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT "La columna revisado ya existe" AS mensaje'
);

PREPARE stmt_revisado FROM @sql_revisado;
EXECUTE stmt_revisado;
DEALLOCATE PREPARE stmt_revisado;


-- =====================================================
-- 3. CREAR TABLA pedido_revision
-- =====================================================

CREATE TABLE IF NOT EXISTS pedido_revision (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    observacion VARCHAR(500) NOT NULL,
    fecha_revision DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_pedido_revision_pedido
        UNIQUE (pedido_id),

    CONSTRAINT fk_pedido_revision_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedido(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- =====================================================
-- 4. VERIFICAR ESTRUCTURA DE LAS TABLAS
-- =====================================================

DESCRIBE pedido;

DESCRIBE pedido_revision;


-- =====================================================
-- 5. VERIFICAR RELACION ENTRE PEDIDO Y REVISION
-- =====================================================

SELECT
    p.id AS pedido_id,
    p.cliente,
    p.estado,
    p.revisado,
    pr.id AS revision_id,
    pr.observacion,
    pr.fecha_revision
FROM pedido p
LEFT JOIN pedido_revision pr
    ON p.id = pr.pedido_id
ORDER BY p.id;
