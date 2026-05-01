<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260404154250 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE article CHANGE date_publication date_publication DATETIME DEFAULT NULL, CHANGE image image VARCHAR(255) DEFAULT NULL, CHANGE region region VARCHAR(100) DEFAULT NULL, CHANGE categorie categorie VARCHAR(100) DEFAULT NULL');
        $this->addSql('ALTER TABLE article RENAME INDEX fk_23a0e6655ab140 TO IDX_23A0E6655AB140');
        $this->addSql('ALTER TABLE commentaire DROP FOREIGN KEY `commentaire_ibfk_1`');
        $this->addSql('ALTER TABLE commentaire DROP FOREIGN KEY `commentaire_ibfk_2`');
        $this->addSql('ALTER TABLE commentaire DROP FOREIGN KEY `commentaire_ibfk_3`');
        $this->addSql('ALTER TABLE commentaire CHANGE contenu contenu LONGTEXT NOT NULL, CHANGE date_commentaire date_commentaire DATETIME DEFAULT NULL');
        $this->addSql('ALTER TABLE commentaire ADD CONSTRAINT FK_67F068BC1D1C63B3 FOREIGN KEY (utilisateur) REFERENCES utilisateur (id)');
        $this->addSql('ALTER TABLE commentaire ADD CONSTRAINT FK_67F068BC7294869C FOREIGN KEY (article_id) REFERENCES article (id)');
        $this->addSql('ALTER TABLE commentaire ADD CONSTRAINT FK_67F068BC727ACA70 FOREIGN KEY (parent_id) REFERENCES commentaire (id)');
        $this->addSql('ALTER TABLE commentaire RENAME INDEX utilisateur TO IDX_67F068BC1D1C63B3');
        $this->addSql('ALTER TABLE commentaire RENAME INDEX article_id TO IDX_67F068BC7294869C');
        $this->addSql('ALTER TABLE commentaire RENAME INDEX parent_id TO IDX_67F068BC727ACA70');
        $this->addSql('ALTER TABLE utilisateur CHANGE type type VARCHAR(50) DEFAULT NULL, CHANGE is_verified is_verified TINYINT DEFAULT NULL, CHANGE verification_code verification_code VARCHAR(10) DEFAULT NULL');
        $this->addSql('ALTER TABLE messenger_messages CHANGE delivered_at delivered_at DATETIME DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE article CHANGE date_publication date_publication DATETIME DEFAULT \'NULL\', CHANGE image image VARCHAR(255) DEFAULT \'NULL\', CHANGE region region VARCHAR(100) DEFAULT \'NULL\', CHANGE categorie categorie VARCHAR(100) DEFAULT \'NULL\'');
        $this->addSql('ALTER TABLE article RENAME INDEX idx_23a0e6655ab140 TO FK_23A0E6655AB140');
        $this->addSql('ALTER TABLE commentaire DROP FOREIGN KEY FK_67F068BC1D1C63B3');
        $this->addSql('ALTER TABLE commentaire DROP FOREIGN KEY FK_67F068BC7294869C');
        $this->addSql('ALTER TABLE commentaire DROP FOREIGN KEY FK_67F068BC727ACA70');
        $this->addSql('ALTER TABLE commentaire CHANGE contenu contenu TEXT NOT NULL, CHANGE date_commentaire date_commentaire DATETIME DEFAULT \'current_timestamp()\'');
        $this->addSql('ALTER TABLE commentaire ADD CONSTRAINT `commentaire_ibfk_1` FOREIGN KEY (utilisateur) REFERENCES utilisateur (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE commentaire ADD CONSTRAINT `commentaire_ibfk_2` FOREIGN KEY (article_id) REFERENCES article (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE commentaire ADD CONSTRAINT `commentaire_ibfk_3` FOREIGN KEY (parent_id) REFERENCES commentaire (id) ON UPDATE CASCADE ON DELETE CASCADE');
        $this->addSql('ALTER TABLE commentaire RENAME INDEX idx_67f068bc7294869c TO article_id');
        $this->addSql('ALTER TABLE commentaire RENAME INDEX idx_67f068bc727aca70 TO parent_id');
        $this->addSql('ALTER TABLE commentaire RENAME INDEX idx_67f068bc1d1c63b3 TO utilisateur');
        $this->addSql('ALTER TABLE messenger_messages CHANGE delivered_at delivered_at DATETIME DEFAULT \'NULL\'');
        $this->addSql('ALTER TABLE utilisateur CHANGE type type VARCHAR(50) DEFAULT \'NULL\', CHANGE is_verified is_verified TINYINT DEFAULT 0, CHANGE verification_code verification_code VARCHAR(10) DEFAULT \'NULL\'');
    }
}
