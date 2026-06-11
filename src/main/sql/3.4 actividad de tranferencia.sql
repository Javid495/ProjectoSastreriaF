CREATE DATABASE analizador_letras;
USE analizador_letras;
CREATE TABLE Usuarios (
    usuario_id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    edad INT,
    pais VARCHAR(50),
    fecha_registro DATE
);
CREATE TABLE Artistas (
    artista_id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    nacionalidad VARCHAR(50),
    genero VARCHAR(50)
);
CREATE TABLE Canciones (
    cancion_id INT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(150) NOT NULL,
    artista_id INT,
    album VARCHAR(100),
    genero VARCHAR(50),
    anio INT,
    FOREIGN KEY (artista_id) REFERENCES Artistas(artista_id)
);
CREATE TABLE Letras (
    letra_id INT PRIMARY KEY AUTO_INCREMENT,
    cancion_id INT UNIQUE NOT NULL,
    texto TEXT NOT NULL,
    idioma VARCHAR(20),
    num_palabras INT,
    num_estrofas INT,
    FOREIGN KEY (cancion_id) REFERENCES Canciones(cancion_id)
);
CREATE TABLE Analisis (
    analisis_id INT PRIMARY KEY AUTO_INCREMENT,
    usuario_id INT,
    cancion_id INT,
    fecha_analisis DATE,
    sentimiento_general VARCHAR(50),
    complejidad_lexica DECIMAL(5,2),
    FOREIGN KEY (usuario_id) REFERENCES Usuarios(usuario_id),
    FOREIGN KEY (cancion_id) REFERENCES Canciones(cancion_id)
);
INSERT INTO Usuarios (nombre, email, edad, pais) VALUES
('Ana López', 'ana@email.com', 22, 'Colombia'),
('Luis Martínez', 'luis@email.com', 19, 'Colombia'),
('María González', 'maria@email.com', 25, 'México'),
('Carlos Rivera', 'carlos@email.com', 28, 'Colombia'),
('Sofía Ramírez', 'sofia@email.com', 17, 'Argentina'),
('Diego Morales', 'diego@email.com', 31, 'Colombia'),
('Valentina Castro', 'valentina@email.com', 24, 'Chile'),
('Juan Pérez', 'juan@email.com', 20, 'Colombia'),
('Camila Torres', 'camila@email.com', 23, 'Perú'),
('Andrés Vargas', 'andres@email.com', 26, 'Colombia');

INSERT INTO Artistas (nombre, nacionalidad, genero) VALUES
('Blessd', 'Colombia', 'Reggaetón'),
('Kris R', 'Colombia', 'Trap'),
('Feid', 'Colombia', 'Reggaetón'),
('Karol G', 'Colombia', 'Reggaetón'),
('Quevedo', 'España', 'Trap'),
('Bad Bunny', 'Puerto Rico', 'Reggaetón'),
('J Balvin', 'Colombia', 'Reggaetón'),
('Maluma', 'Colombia', 'Reggaetón'),
('Yng Lvcas', 'México', 'Reggaetón'),
('Ryan Castro', 'Colombia', 'Reggaetón');

INSERT INTO Canciones (titulo, artista_id, album, genero, anio) VALUES
('Soltera', 1, 'Blessd', 'Reggaetón', 2024),
('Tuki Tuki', 2, 'Kris R', 'Trap', 2025),
('Ferxxo 100', 3, 'Ferxxo', 'Reggaetón', 2023),
('Provenza', 4, 'KG', 'Reggaetón', 2022),
('Columbia', 5, 'Quevedo', 'Trap', 2024),
('Tití Me Preguntó', 6, 'Un Verano Sin Ti', 'Reggaetón', 2022),
('Ginza', 7, 'La Familia', 'Reggaetón', 2015),
('Hawái', 8, 'Papi Juancho', 'Reggaetón', 2020),
('La Bebé', 9, 'Single', 'Reggaetón', 2023),
('Mujer', 10, 'Single', 'Reggaetón', 2024);

INSERT INTO Letras (cancion_id, texto, num_palabras, num_estrofas) VALUES
(1, 'Estoy soltera...', 245, 3),
(2, 'Tuki tuki...', 180, 2),
(3, 'Ferxxo en la casa...', 310, 4),
(4, 'Provenza...', 220, 3),
(5, 'Columbia me llama...', 190, 3),
(6, 'Tití me preguntó...', 280, 4),
(7, 'En Ginza...', 150, 2),
(8, 'Hawái...', 200, 3),
(9, 'La bebé...', 260, 3),
(10, 'Mujer...', 170, 2);

INSERT INTO Analisis (usuario_id, cancion_id, sentimiento_general, complejidad_lexica) VALUES
(1, 1, 'Positivo', 6.8),
(2, 2, 'Energético', 5.4),
(3, 3, 'Melancólico', 7.2),
(4, 4, 'Alegre', 6.1),
(5, 5, 'Nostálgico', 8.0),
(6, 6, 'Curioso', 6.5),
(7, 7, 'Fiesta', 5.9),
(8, 8, 'Romántico', 7.1),
(9, 9, 'Pasional', 6.3),
(10, 10, 'Amor', 6.7);
-- Consulta 1: INNER JOIN
-- sirve para: Mostrar canciones con su artista
-- Por qué se utiliza el INNER JOIN aca, es para relacionar canciones con el nombre del artista
SELECT c.titulo, a.nombre AS artista
FROM Canciones c
INNER JOIN Artistas a ON c.artista_id = a.artista_id;

-- Consulta 2: INNER JOIN
-- Para: Mostrar canciones con su análisis de sentimiento
-- Por qué: Ver el sentimiento asociado a cada canción
SELECT c.titulo, ar.sentimiento_general
FROM Canciones c
INNER JOIN Analisis ar ON c.cancion_id = ar.cancion_id;

-- Consulta 3: LEFT JOIN
-- Para: Mostrar todas las canciones y sus letras (si existen)
-- Por qué: Ver qué canciones tienen letra registrada
SELECT c.titulo, l.num_palabras
FROM Canciones c
LEFT JOIN Letras l ON c.cancion_id = l.cancion_id;