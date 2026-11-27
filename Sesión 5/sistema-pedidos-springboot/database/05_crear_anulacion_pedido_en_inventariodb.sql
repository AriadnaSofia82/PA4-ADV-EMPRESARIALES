USE inventariodb;

CREATE TABLE `anulacion_pedido` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad_devuelta` int NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `motivo` varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pedido_id` bigint NOT NULL,
  `producto_codigo` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_anulacion_pedido_pedido_id` (`pedido_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
