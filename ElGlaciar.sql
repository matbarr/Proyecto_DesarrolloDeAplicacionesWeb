DROP DATABASE IF EXISTS glaciar_gestion;

CREATE DATABASE glaciar_gestion
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE glaciar_gestion;

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    telefono VARCHAR(255) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT ck_usuarios_rol
        CHECK (rol IN ('CLIENTE', 'ADMINISTRADOR'))
);

CREATE TABLE productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    presentacion VARCHAR(255) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    cantidad INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT ck_productos_precio
        CHECK (precio > 0),

    CONSTRAINT ck_productos_cantidad
        CHECK (cantidad >= 0)
);

CREATE TABLE direcciones_entrega (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    provincia VARCHAR(255) NOT NULL,
    canton VARCHAR(255) NOT NULL,
    distrito VARCHAR(255) NOT NULL,
    direccion_exacta VARCHAR(255) NOT NULL,

    CONSTRAINT fk_direcciones_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE TABLE carritos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE,
    creado_en DATETIME NOT NULL,

    CONSTRAINT fk_carritos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE TABLE detalles_carrito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    carrito_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,

    CONSTRAINT uq_detalle_carrito_producto
        UNIQUE (carrito_id, producto_id),

    CONSTRAINT fk_detalle_carrito_carrito
        FOREIGN KEY (carrito_id)
        REFERENCES carritos(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_detalle_carrito_producto
        FOREIGN KEY (producto_id)
        REFERENCES productos(id)
        ON DELETE CASCADE,

    CONSTRAINT ck_detalle_carrito_cantidad
        CHECK (cantidad > 0)
);

CREATE TABLE pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    direccion_id BIGINT NOT NULL,
    fecha DATETIME NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    observaciones VARCHAR(600),

    CONSTRAINT fk_pedidos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_pedidos_direccion
        FOREIGN KEY (direccion_id)
        REFERENCES direcciones_entrega(id),

    CONSTRAINT ck_pedidos_estado
        CHECK (
            estado IN (
                'PENDIENTE',
                'EN_PREPARACION',
                'LISTO',
                'ENTREGADO'
            )
        )
);

CREATE TABLE detalles_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_detalle_pedido_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES pedidos(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_detalle_pedido_producto
        FOREIGN KEY (producto_id)
        REFERENCES productos(id),

    CONSTRAINT ck_detalle_pedido_cantidad
        CHECK (cantidad > 0)
);

CREATE TABLE consultas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    asunto VARCHAR(255) NOT NULL,
    abierta BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME NOT NULL,
    ultimo_leido_cliente DATETIME,
    ultimo_leido_admin DATETIME,

    CONSTRAINT fk_consultas_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE TABLE mensajes_consulta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consulta_id BIGINT NOT NULL,
    emisor_id BIGINT NOT NULL,
    contenido VARCHAR(2000) NOT NULL,
    fecha DATETIME NOT NULL,

    CONSTRAINT fk_mensajes_consulta
        FOREIGN KEY (consulta_id)
        REFERENCES consultas(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_mensajes_emisor
        FOREIGN KEY (emisor_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_productos_activo
ON productos(activo);

CREATE INDEX idx_pedidos_usuario
ON pedidos(usuario_id);

CREATE INDEX idx_pedidos_estado
ON pedidos(estado);

CREATE INDEX idx_direcciones_usuario
ON direcciones_entrega(usuario_id);

CREATE INDEX idx_consultas_cliente
ON consultas(cliente_id);

CREATE INDEX idx_mensajes_consulta
ON mensajes_consulta(consulta_id);

INSERT INTO usuarios
(nombre, correo, telefono, contrasena, rol, activo)
VALUES
(
    'Administrador',
    'admin@glaciar.com',
    '88888888',
    'admin123',
    'ADMINISTRADOR',
    TRUE
);

INSERT INTO productos
(nombre, presentacion, precio, cantidad, activo)
VALUES
(
    'Hielo para copos',
    'Bolsa 2 kg',
    1200.00,
    100,
    TRUE
),
(
    'Hielo en escama',
    'Bolsa 5 kg',
    2500.00,
    100,
    TRUE
),
(
    'Hielo en cubo',
    'Bolsa 5 kg',
    2200.00,
    100,
    TRUE
);