-- insert admin (username a, password aa)
INSERT INTO IWUser (id, enabled, roles, username, password)
VALUES (1, TRUE, 'ADMIN,USER', 'a',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W');
INSERT INTO IWUser (id, enabled, roles, username, password)
VALUES (2, TRUE, 'USER', 'b',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W');

-- start id numbering from a value that is larger than any assigned above
ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;



INSERT INTO Product (id, ean, imageurl, name) VALUES
(1, '8410000000012', 'https://picsum.photos/200?sig=1', 'Leche Semidesnatada'),
(2, '8410000000013', 'https://picsum.photos/200?sig=1', 'Leche Entera');

INSERT INTO Supermarket (id, name, info) VALUES 
(1, 'Mercadona', 'Supermercado de confianza'),
(2, 'Carrefour', 'Supermercado de confianza'),
(3, 'Lidl', 'Supermercado de confianza'),
(4, 'Dia', 'Supermercado de confianza'),
(5, 'Alcampo', 'Supermercado de confianza');

INSERT INTO Product_Supermarket (id, price, date, product_id, supermarket_id) VALUES 
(1, 1.5, '2024-06-01', 1, 1),
(2, 1.6, '2024-06-01', 1, 2),
(3, 1.4, '2024-06-01', 1, 3),
(4, 1.4, '2024-06-01', 1, 4),
(5, 1.4, '2024-06-01', 2, 5);




