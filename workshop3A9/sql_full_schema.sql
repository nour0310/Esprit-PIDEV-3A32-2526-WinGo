-- Full database schema for wingo project
-- Run this if you need to create/recreate tables

USE wingo;

-- Drop existing if you want a clean start (optional)
-- DROP TABLE IF EXISTS profil;
-- DROP TABLE IF EXISTS produit;
-- DROP TABLE IF EXISTS utilisateur;

CREATE TABLE IF NOT EXISTS utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    email VARCHAR(150),
    mot_de_passe VARCHAR(100),
    type VARCHAR(50),
    telephone VARCHAR(20),
    age INT
);

CREATE TABLE IF NOT EXISTS profil (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bio TEXT,
    image VARCHAR(255),
    utilisateur_id INT
);

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
