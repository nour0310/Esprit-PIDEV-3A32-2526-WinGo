<?php

require __DIR__ . '/../vendor/autoload.php';

use Symfony\Component\Dotenv\Dotenv;

$dotenv = new Dotenv();
$dotenv->load(__DIR__ . '/../.env');

$dbUrl = $_ENV['DATABASE_URL'];
$parsedUrl = parse_url($dbUrl);

$host = $parsedUrl['host'];
$user = $parsedUrl['user'];
$pass = $parsedUrl['pass'] ?? '';
$db   = ltrim($parsedUrl['path'], '/');

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8mb4", $user, $pass, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION
    ]);

    echo "Connexion rÃ©ussie Ã  $db\n";

    $queries = [
        "ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS face_descriptor LONGTEXT DEFAULT NULL;",
        "ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS photo VARCHAR(191) DEFAULT NULL;",
        "ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS updated_at DATETIME DEFAULT NULL;"
    ];

    foreach ($queries as $query) {
        try {
            $pdo->exec($query);
            echo "ExÃ©cutÃ© : $query\n";
        } catch (Exception $e) {
            echo "Erreur sur $query : " . $e->getMessage() . "\n";
        }
    }

    echo "Base de donnÃ©es mise Ã  jour !\n";

} catch (Exception $e) {
    echo "Erreur fatale : " . $e->getMessage() . "\n";
}
