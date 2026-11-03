-- BD "tienda": pegar dentro de la BD en phpMyAdmin (pestaña SQL) y Ejecutar

DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS categoria;

CREATE TABLE categoria (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    descripcion   VARCHAR(255),
    es_suplemento BOOLEAN NOT NULL
) ENGINE=InnoDB;

CREATE TABLE producto (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(120) NOT NULL,
    precio         DECIMAL(10,2) NOT NULL,
    stock          INT NOT NULL,
    formato        VARCHAR(50) NOT NULL,
    sabor_material VARCHAR(50) NOT NULL,
    categoria_id   INT NOT NULL,
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria (id)
) ENGINE=InnoDB;

CREATE TABLE pedido (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    cliente_email VARCHAR(100) NOT NULL,
    cantidad      INT NOT NULL,
    fecha_pedido  DATE NOT NULL,
    total         DECIMAL(10,2) NOT NULL,
    producto_id   INT NOT NULL,
    CONSTRAINT fk_pedido_producto
        FOREIGN KEY (producto_id) REFERENCES producto (id)
) ENGINE=InnoDB;

-- ------------------- DATOS DE PRUEBA -------------------

INSERT INTO categoria (nombre, descripcion, es_suplemento) VALUES
    ('Proteínas',     'Proteínas en polvo para recuperación muscular', FALSE),
    ('Creatina',      'Creatina monohidrato y variantes',              TRUE),
    ('Vitaminas',     'Multivitaminas y complejos B',                  TRUE),
    ('Bebidas',       'Bebidas isotónicas y energéticas',              FALSE);

INSERT INTO producto (nombre, precio, stock, formato, sabor_material, categoria_id) VALUES
    -- Proteínas (1)
    ('Whey Isolate',       39.90, 25, '1 kg',   'Chocolate',   1),
    ('Caseína Micelar',    34.50, 15, '1 kg',   'Vainilla',    1),
    ('Proteína Vegana',    29.99,  8, '750 g',  'Plátano',     1),
    ('Proteína Clear',     27.50,  0, '500 g',  'Lima-Limón',  1),
    -- Creatina (2)
    ('Creatina Monohidrato', 19.99, 40, '500 g', 'Sin sabor',  2),
    ('Creatina HCL',         24.99, 12, '300 g', 'Frutas',     2),
    -- Vitaminas (3)
    ('Multivitamínico',    15.90, 30, '90 compr.', 'Sin sabor', 3),
    ('Vitamina C',         9.99,   0, '60 compr.', 'Naranja',   3),
    -- Bebidas (4)
    ('Isotónica',          1.80,  60, '750 ml', 'Frutas',      4),
    ('Bebida Energética',  2.25,  45, '330 ml', 'Naranja',     4);

INSERT INTO pedido (cliente_email, cantidad, fecha_pedido, total, producto_id) VALUES
    ('ana@example.com',    2, '2026-11-01',  79.80, 1),
    ('luis@example.com',   1, '2026-11-01',  19.99, 5),
    ('ana@example.com',    3, '2026-11-02',  47.70, 6),
    ('martha@example.com', 1, '2026-11-02',  15.90, 7),
    ('luis@example.com',   5, '2026-11-03',   9.00, 9),
    ('carlos@example.com', 2, '2026-11-03',   4.50, 10);
