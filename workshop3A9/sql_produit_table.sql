-- Run this SQL in your MySQL wingo database to create the produit table

USE wingo;

CREATE TABLE IF NOT EXISTS produit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(200) NOT NULL,
    prix DOUBLE NOT NULL DEFAULT 0,
    stock INT NOT NULL DEFAULT 0,
    categorie VARCHAR(100),
    region VARCHAR(100),
    description TEXT,
    image VARCHAR(500)
);
