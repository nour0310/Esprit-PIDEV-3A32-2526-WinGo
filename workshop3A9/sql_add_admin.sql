-- Add admin account to log in
-- Run this in MySQL

USE wingo;

INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, type, telephone, age)
VALUES ('Admin', 'User', 'admin@wingo.com', 'admin123', 'admin', '12345678', 30);
