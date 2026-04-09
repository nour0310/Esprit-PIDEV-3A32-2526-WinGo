<?php
require 'vendor/autoload.php';
(new Symfony\Component\Dotenv\Dotenv())->bootEnv(dirname(__DIR__).'/web/.env');
$kernel = new App\Kernel('dev', true);
$kernel->boot();
$twig = $kernel->getContainer()->get('twig');
$extension = $twig->getExtension(Symfony\Bridge\Twig\Extension\FormExtension::class);
print_r($extension->renderer->getEngine()->getTheme());

