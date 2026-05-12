<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260510203635 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE chat_message (id INT AUTO_INCREMENT NOT NULL, message LONGTEXT NOT NULL, created_at DATETIME NOT NULL, event_id INT NOT NULL, user_id INT NOT NULL, INDEX IDX_FAB3FC1671F7E88B (event_id), INDEX IDX_FAB3FC16A76ED395 (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE event (id_event INT AUTO_INCREMENT NOT NULL, title VARCHAR(255) NOT NULL, description LONGTEXT NOT NULL, date_event DATE NOT NULL, start_time VARCHAR(255) NOT NULL, location VARCHAR(255) NOT NULL, event_type VARCHAR(100) NOT NULL, season VARCHAR(50) NOT NULL, capacity INT NOT NULL, available_places INT NOT NULL, status VARCHAR(50) NOT NULL, image_event VARCHAR(255) NOT NULL, price DOUBLE PRECISION NOT NULL, is_passed TINYINT DEFAULT NULL, feedbacks LONGTEXT DEFAULT NULL, PRIMARY KEY (id_event)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE notification_commerce (id INT AUTO_INCREMENT NOT NULL, title VARCHAR(255) NOT NULL, message LONGTEXT NOT NULL, type VARCHAR(50) NOT NULL, is_read TINYINT NOT NULL, created_at DATETIME NOT NULL, target_role VARCHAR(50) DEFAULT NULL, target_user_id INT DEFAULT NULL, link VARCHAR(255) DEFAULT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE participation (id_participation INT AUTO_INCREMENT NOT NULL, id_user INT NOT NULL, date_participation DATETIME NOT NULL, statut VARCHAR(50) NOT NULL, nom_participant VARCHAR(100) NOT NULL, prenom_participant VARCHAR(100) NOT NULL, email_participant VARCHAR(150) NOT NULL, telephone VARCHAR(30) NOT NULL, nombre_places INT NOT NULL, token VARCHAR(255) DEFAULT NULL, is_used TINYINT DEFAULT 0 NOT NULL, unit_price DOUBLE PRECISION DEFAULT NULL, total_price DOUBLE PRECISION DEFAULT NULL, id_event INT DEFAULT NULL, UNIQUE INDEX UNIQ_AB55E24F5F37A13B (token), INDEX IDX_AB55E24FD52B4B97 (id_event), PRIMARY KEY (id_participation)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE promo_code (id INT AUTO_INCREMENT NOT NULL, code VARCHAR(255) NOT NULL, is_used INT NOT NULL, user_id INT DEFAULT NULL, INDEX IDX_3D8C939EA76ED395 (user_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE promotion (id INT AUTO_INCREMENT NOT NULL, code VARCHAR(20) NOT NULL, type VARCHAR(30) NOT NULL, id_user INT NOT NULL, is_used TINYINT DEFAULT 0 NOT NULL, date_expiration DATETIME NOT NULL, created_at DATETIME NOT NULL, UNIQUE INDEX UNIQ_C11D7DD177153098 (code), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE reward (id INT AUTO_INCREMENT NOT NULL, id_user INT NOT NULL, type_jeu VARCHAR(20) NOT NULL, reward_type VARCHAR(30) NOT NULL, played_at DATETIME NOT NULL, promotion_id INT DEFAULT NULL, INDEX IDX_4ED17253139DF194 (promotion_id), INDEX idx_reward_user_date (id_user, played_at), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE chat_message ADD CONSTRAINT FK_FAB3FC1671F7E88B FOREIGN KEY (event_id) REFERENCES event (id_event) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE chat_message ADD CONSTRAINT FK_FAB3FC16A76ED395 FOREIGN KEY (user_id) REFERENCES utilisateur (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE participation ADD CONSTRAINT FK_AB55E24FD52B4B97 FOREIGN KEY (id_event) REFERENCES event (id_event) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE promo_code ADD CONSTRAINT FK_3D8C939EA76ED395 FOREIGN KEY (user_id) REFERENCES utilisateur (id)');
        $this->addSql('ALTER TABLE reward ADD CONSTRAINT FK_4ED17253139DF194 FOREIGN KEY (promotion_id) REFERENCES promotion (id)');
        $this->addSql('ALTER TABLE article ADD CONSTRAINT FK_23A0E6655AB140 FOREIGN KEY (auteur) REFERENCES utilisateur (id) ON DELETE SET NULL');
        $this->addSql('CREATE INDEX IDX_23A0E6655AB140 ON article (auteur)');
        $this->addSql('ALTER TABLE article_tag DROP FOREIGN KEY `FK_919694F97294869C`');
        $this->addSql('ALTER TABLE article_tag DROP FOREIGN KEY `FK_919694F9BAD26311`');
        $this->addSql('ALTER TABLE article_tag ADD CONSTRAINT FK_919694F97294869C FOREIGN KEY (article_id) REFERENCES article (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE article_tag ADD CONSTRAINT FK_919694F9BAD26311 FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE commande ADD cause_annulation VARCHAR(255) DEFAULT NULL, CHANGE status status ENUM(\'panier\',\'en_cours\',\'livree\',\'annulee\') NOT NULL DEFAULT \'en_cours\', CHANGE total total NUMERIC(10, 2) DEFAULT 0 NOT NULL');
        $this->addSql('ALTER TABLE commentaire ADD CONSTRAINT FK_67F068BC1D1C63B3 FOREIGN KEY (utilisateur) REFERENCES utilisateur (id)');
        $this->addSql('ALTER TABLE commentaire ADD CONSTRAINT FK_67F068BC7294869C FOREIGN KEY (article_id) REFERENCES article (id)');
        $this->addSql('ALTER TABLE commentaire ADD CONSTRAINT FK_67F068BC727ACA70 FOREIGN KEY (parent_id) REFERENCES commentaire (id)');
        $this->addSql('CREATE INDEX IDX_67F068BC1D1C63B3 ON commentaire (utilisateur)');
        $this->addSql('CREATE INDEX IDX_67F068BC7294869C ON commentaire (article_id)');
        $this->addSql('CREATE INDEX IDX_67F068BC727ACA70 ON commentaire (parent_id)');
        $this->addSql('ALTER TABLE reservation ADD stars INT DEFAULT NULL, ADD comment LONGTEXT DEFAULT NULL, ADD price INT DEFAULT NULL, ADD user_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE reservation ADD CONSTRAINT FK_42C84955A76ED395 FOREIGN KEY (user_id) REFERENCES utilisateur (id)');
        $this->addSql('CREATE INDEX IDX_42C84955A76ED395 ON reservation (user_id)');
        $this->addSql('ALTER TABLE transport ADD user_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE transport ADD CONSTRAINT FK_66AB212EA76ED395 FOREIGN KEY (user_id) REFERENCES utilisateur (id)');
        $this->addSql('CREATE INDEX IDX_66AB212EA76ED395 ON transport (user_id)');
        $this->addSql('ALTER TABLE utilisateur ADD photo VARCHAR(255) DEFAULT NULL, DROP reset_token, DROP api_token, DROP photoUrl, DROP faceDescriptor, DROP verified, DROP image, CHANGE verification_code verification_code VARCHAR(10) DEFAULT NULL, CHANGE genre genre VARCHAR(20) DEFAULT NULL, CHANGE reset_token_expires_at updated_at DATETIME DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE chat_message DROP FOREIGN KEY FK_FAB3FC1671F7E88B');
        $this->addSql('ALTER TABLE chat_message DROP FOREIGN KEY FK_FAB3FC16A76ED395');
        $this->addSql('ALTER TABLE participation DROP FOREIGN KEY FK_AB55E24FD52B4B97');
        $this->addSql('ALTER TABLE promo_code DROP FOREIGN KEY FK_3D8C939EA76ED395');
        $this->addSql('ALTER TABLE reward DROP FOREIGN KEY FK_4ED17253139DF194');
        $this->addSql('DROP TABLE chat_message');
        $this->addSql('DROP TABLE event');
        $this->addSql('DROP TABLE notification_commerce');
        $this->addSql('DROP TABLE participation');
        $this->addSql('DROP TABLE promo_code');
        $this->addSql('DROP TABLE promotion');
        $this->addSql('DROP TABLE reward');
        $this->addSql('ALTER TABLE article DROP FOREIGN KEY FK_23A0E6655AB140');
        $this->addSql('DROP INDEX IDX_23A0E6655AB140 ON article');
        $this->addSql('ALTER TABLE article_tag DROP FOREIGN KEY FK_919694F97294869C');
        $this->addSql('ALTER TABLE article_tag DROP FOREIGN KEY FK_919694F9BAD26311');
        $this->addSql('ALTER TABLE article_tag ADD CONSTRAINT `FK_919694F97294869C` FOREIGN KEY (article_id) REFERENCES article (id)');
        $this->addSql('ALTER TABLE article_tag ADD CONSTRAINT `FK_919694F9BAD26311` FOREIGN KEY (tag_id) REFERENCES tag (id)');
        $this->addSql('ALTER TABLE commande DROP cause_annulation, CHANGE status status ENUM(\'panier\', \'en_cours\', \'livree\', \'annulee\') DEFAULT \'en_cours\' NOT NULL, CHANGE total total NUMERIC(10, 2) DEFAULT \'0.00\' NOT NULL');
        $this->addSql('ALTER TABLE commentaire DROP FOREIGN KEY FK_67F068BC1D1C63B3');
        $this->addSql('ALTER TABLE commentaire DROP FOREIGN KEY FK_67F068BC7294869C');
        $this->addSql('ALTER TABLE commentaire DROP FOREIGN KEY FK_67F068BC727ACA70');
        $this->addSql('DROP INDEX IDX_67F068BC1D1C63B3 ON commentaire');
        $this->addSql('DROP INDEX IDX_67F068BC7294869C ON commentaire');
        $this->addSql('DROP INDEX IDX_67F068BC727ACA70 ON commentaire');
        $this->addSql('ALTER TABLE reservation DROP FOREIGN KEY FK_42C84955A76ED395');
        $this->addSql('DROP INDEX IDX_42C84955A76ED395 ON reservation');
        $this->addSql('ALTER TABLE reservation DROP stars, DROP comment, DROP price, DROP user_id');
        $this->addSql('ALTER TABLE transport DROP FOREIGN KEY FK_66AB212EA76ED395');
        $this->addSql('DROP INDEX IDX_66AB212EA76ED395 ON transport');
        $this->addSql('ALTER TABLE transport DROP user_id');
        $this->addSql('ALTER TABLE utilisateur ADD api_token VARCHAR(255) DEFAULT NULL, ADD photoUrl VARCHAR(500) DEFAULT NULL, ADD faceDescriptor TEXT DEFAULT NULL, ADD verified TINYINT DEFAULT 0, ADD image VARCHAR(255) DEFAULT \'default.png\', CHANGE verification_code verification_code VARCHAR(255) DEFAULT NULL, CHANGE genre genre VARCHAR(15) DEFAULT NULL, CHANGE photo reset_token VARCHAR(255) DEFAULT NULL, CHANGE updated_at reset_token_expires_at DATETIME DEFAULT NULL');
    }
}
