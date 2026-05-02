<?php

namespace App\Command;

use Doctrine\DBAL\Connection;
use Doctrine\DBAL\Schema\AbstractSchemaManager;
use Doctrine\DBAL\Schema\Column;
use Doctrine\DBAL\Schema\Table;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:generate:entities',
    description: 'Automatically generates entity classes from the database schema',
)]
class GenerateEntitiesCommand extends Command
{
    private Connection $connection;
    private ?AbstractSchemaManager $schemaManager = null;
    private array $generatedRelations = [];

    /**
     * Constructor.
     *
     * @param Connection $connection The database connection instance.
     */
    public function __construct(Connection $connection)
    {
        parent::__construct();

        $this->connection = $connection;
    }

    /**
     * Executes the command to generate entity classes.
     *
     * @param InputInterface $input Input interface.
     * @param OutputInterface $output Output interface.
     *
     * @return int Command exit status.
     */
    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        $io->title('Generating Entity Classes from Database...');

        try {
            $schemaManager = $this->getSchemaManager();
            $tables = $schemaManager->listTables();
        } catch (\Exception $e) {
            $io->error('Failed to retrieve database schema: ' . $e->getMessage());

            return Command::FAILURE;
        }

        $oneToManyRelations = [];
        $manyToOneRelationsName = [];
        $oneToManyRelationsName = [];

        $tableRelationsCount = [];

        foreach ($tables as $table) {
            $foreignKeys = $this->getForeignKeys([$table->getName()]);
            $tableRelationsCount[$table->getName()] = count($foreignKeys);
        }

        usort($tables, function (Table $a, Table $b) use ($tableRelationsCount) {
            return $tableRelationsCount[$a->getName()] <=> $tableRelationsCount[$b->getName()];
        });

        foreach ($tables as $table) {
            $this->generateEntity($table, $oneToManyRelations, $manyToOneRelationsName, $oneToManyRelationsName);

            $io->success('Generated: src/Entity/' . ucfirst($table->getName()) . '.php');
        }

        foreach ($tables as $table) {
            $this->generateEntity($table, $oneToManyRelations, $manyToOneRelationsName, $oneToManyRelationsName);

            $io->success('Relations Added: src/Entity/' . ucfirst($table->getName()) . '.php');
        }

        $io->success('Entities successfully generated in src/Entity/');

        return Command::SUCCESS;
    }

    private function getSchemaManager(): AbstractSchemaManager
    {
        if ($this->schemaManager === null) {
            $this->schemaManager = $this->connection->createSchemaManager();
        }

        return $this->schemaManager;
    }

    /**
     * Generates an entity class from a database table.
     *
     * @param Table $table The database table.
     * @param array $oneToManyRelations Reference to OneToMany relations.
     * @param array $manyToOneRelationsName Reference to ManyToOne relations.
     * @param array $oneToManyRelationsName Reference to OneToMany relation names.
     */
    private function generateEntity(
        Table $table,
        array &$oneToManyRelations,
        array &$manyToOneRelationsName,
        array &$oneToManyRelationsName
    ): void {
        $className = ucfirst($table->getName());
        $entityCode = "<?php\n\nnamespace App\\Entity;\n\nuse Doctrine\\ORM\\Mapping as ORM;\n\n";

        $imports = $this->generateImports($manyToOneRelationsName, $oneToManyRelationsName, $className);
        $entityCode .= $imports . "\n";

        $entityCode .= "#[ORM\\Entity]\n";
        $entityCode .= "class $className\n{\n";

        $primaryKeys = $table->getPrimaryKey()?->getColumns() ?? [];
        $foreignKeys = $this->getForeignKeys([$table->getName()]);

        foreach ($table->getColumns() as $column) {
            $entityCode .= $this->generateProperty(
                $column,
                $primaryKeys,
                $foreignKeys,
                $className,
                $oneToManyRelations,
                $manyToOneRelationsName,
                $oneToManyRelationsName
            );
        }

        foreach ($table->getColumns() as $column) {
            $entityCode .= $this->generateGettersAndSetters($column);
        }

        if (isset($oneToManyRelations[$className])) {
            $processedRelations = [];

            foreach ($oneToManyRelations[$className] as $relation) {
                if (!in_array($relation, $processedRelations, true)) {
                    $entityCode .= $relation;
                    $processedRelations[] = $relation;

                    $relationArray = $this->parseRelationAnnotation($relation);
                    $relationKey = "$className-{$relationArray['mappedBy']}";

                    if (!isset($this->generatedRelations[$relationKey])) {
                        $entityCode .= $this->generateRelationMethods(
                            $className,
                            (string) $relationArray['mappedBy'],
                            (string) $relationArray['targetEntity']
                        );

                        $this->generatedRelations[$relationKey] = true;
                    }
                }
            }
        }

        $entityCode .= "}\n";

        $filePath = __DIR__ . "/../../src/Entity/$className.php";
        file_put_contents($filePath, $entityCode);
    }

    /**
     * Generates necessary import statements based on detected relations.
     *
     * @param array $manyToOneRelationsName ManyToOne relations.
     * @param array $oneToManyRelationsName OneToMany relations.
     * @param string $className The name of the entity class.
     *
     * @return string Formatted import statements.
     */
    private function generateImports(array $manyToOneRelationsName, array $oneToManyRelationsName, string $className): string
    {
        $imports = [];

        foreach ($manyToOneRelationsName as $key => $value) {
            if ($key === $className) {
                $imports[] = "App\\Entity\\$value";
            }
        }

        foreach ($oneToManyRelationsName as $key => $value) {
            if ($key === $className) {
                $imports[] = 'Doctrine\Common\Collections\Collection';
                $imports[] = "App\\Entity\\$value";
            }
        }

        $imports = array_unique($imports);

        if (count($imports) === 0) {
            return '';
        }

        return 'use ' . implode(";\nuse ", $imports) . ";\n";
    }

    /**
     * Retrieves foreign key constraints from the database.
     *
     * @param array $tables List of table names.
     *
     * @return array Associative array of foreign keys.
     */
    public function getForeignKeys(array $tables): array
    {
        $foreignKeys = [];

        $schemaManager = $this->connection->createSchemaManager();
        $dbTables = $schemaManager->listTables();

        foreach ($tables as $tableName) {
            if (in_array($tableName, array_map(fn (Table $table) => $table->getName(), $dbTables), true)) {
                $sql = '
                    SELECT 
                        COLUMN_NAME, 
                        REFERENCED_TABLE_NAME, 
                        REFERENCED_COLUMN_NAME
                    FROM 
                        INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
                    WHERE 
                        TABLE_NAME = :tableName 
                        AND REFERENCED_TABLE_NAME IS NOT NULL
                ';

                $stmt = $this->connection->prepare($sql);
                $stmt->bindValue(':tableName', $tableName);

                $fks = $stmt->executeQuery()->fetchAllAssociative();

                foreach ($fks as $fk) {
                    $foreignKeys[$fk['COLUMN_NAME']] = [
                        'referencedTable' => $fk['REFERENCED_TABLE_NAME'],
                        'referencedColumn' => $fk['REFERENCED_COLUMN_NAME'],
                    ];
                }
            }
        }

        return $foreignKeys;
    }

    /**
     * Generates relation methods for OneToMany and ManyToOne relations.
     *
     * @param string $currentEntity The current entity name.
     * @param string $propertyName The property name representing the relation.
     * @param string $relatedEntity The related entity name.
     *
     * @return string The generated method code.
     */
    private function generateRelationMethods(string $currentEntity, string $propertyName, string $relatedEntity): string
    {
        $collectionType = 'Collection';
        $relatedEntityClass = ucfirst($relatedEntity);
        $relatedEntityVariable = lcfirst($relatedEntity);

        return "
        public function get" . $relatedEntityClass . "s(): $collectionType
        {
            return \$this->" . $relatedEntityVariable . "s;
        }

        public function add{$relatedEntityClass}({$relatedEntityClass} \${$relatedEntityVariable}): self
        {
            if (!\$this->{$relatedEntityVariable}s->contains(\${$relatedEntityVariable})) {
                \$this->{$relatedEntityVariable}s[] = \${$relatedEntityVariable};
                \${$relatedEntityVariable}->set" . ucfirst($propertyName) . "(\$this);
            }

            return \$this;
        }

        public function remove{$relatedEntityClass}({$relatedEntityClass} \${$relatedEntityVariable}): self
        {
            if (\$this->{$relatedEntityVariable}s->removeElement(\${$relatedEntityVariable})) {
                if (\${$relatedEntityVariable}->get" . ucfirst($propertyName) . "() === \$this) {
                    \${$relatedEntityVariable}->set" . ucfirst($propertyName) . "(null);
                }
            }

            return \$this;
        }\n";
    }

    /**
     * Generates entity properties based on database columns.
     *
     * @param Column $column The database column.
     * @param array $primaryKeys List of primary keys.
     * @param array $foreignKeys List of foreign keys.
     * @param string $className The entity class name.
     * @param array $oneToManyRelations Reference to OneToMany relations.
     * @param array $manyToOneRelationsName Reference to ManyToOne relations.
     * @param array $oneToManyRelationsName Reference to OneToMany relation names.
     *
     * @return string The generated property code.
     */
    private function generateProperty(
        Column $column,
        array $primaryKeys,
        array $foreignKeys,
        string $className,
        array &$oneToManyRelations,
        array &$manyToOneRelationsName,
        array &$oneToManyRelationsName
    ): string {
        $columnName = $column->getName();
        $typeClass = get_class($column->getType());
        $length = $column->getLength();
        $isPrimaryKey = in_array($columnName, $primaryKeys, true);
        $isForeignKey = isset($foreignKeys[$columnName]);

        $doctrineType = match ($typeClass) {
            'Doctrine\DBAL\Types\IntegerType' => 'integer',
            'Doctrine\DBAL\Types\BigIntType' => 'bigint',
            'Doctrine\DBAL\Types\SmallIntType' => 'smallint',
            'Doctrine\DBAL\Types\BooleanType' => 'boolean',
            'Doctrine\DBAL\Types\DateTimeType', 'Doctrine\DBAL\Types\TimestampType' => 'datetime',
            'Doctrine\DBAL\Types\DateType' => 'date',
            'Doctrine\DBAL\Types\TextType' => 'text',
            'Doctrine\DBAL\Types\DecimalType', 'Doctrine\DBAL\Types\FloatType', 'Doctrine\DBAL\Types\DoubleType' => 'float',
            'Doctrine\DBAL\Types\StringType', 'Doctrine\DBAL\Types\VarCharType' => 'string',
            default => 'string',
        };

        $lengthAnnotation = ($doctrineType === 'string' && $length) ? ", length: $length" : '';

        $propertyCode = "\n    " . ($isPrimaryKey ? "#[ORM\\Id]\n    " : '');

        if ($isForeignKey) {
            $relatedEntity = $foreignKeys[$columnName]['referencedTable'];
            $relatedClassName = ucfirst($relatedEntity);

            $primaryKeyColumns = $this->getPrimaryKeyColumns($relatedEntity);
            $primaryKeyColumn = $primaryKeyColumns[0] ?? null;

            if ($primaryKeyColumn) {
                $propertyCode .= "    #[ORM\\ManyToOne(targetEntity: $relatedClassName::class, inversedBy: \"" . strtolower($className) . "s\")]\n";
                $propertyCode .= "    #[ORM\\JoinColumn(name: '$columnName', referencedColumnName: '$primaryKeyColumn', onDelete: 'CASCADE')]\n";
                $propertyCode .= "    private $relatedClassName \$$columnName;\n";

                $manyToOneRelationsName[$className] = $relatedClassName;
                $oneToManyRelationsName[$relatedClassName] = $className;

                $oneToManyRelations[$relatedClassName][] = "\n    #[ORM\\OneToMany(mappedBy: \"$columnName\", targetEntity: $className::class)]\n    private Collection \$" . strtolower($className) . "s;\n";
            }
        } else {
            $propertyCode .= "#[ORM\\Column(type: \"$doctrineType\"$lengthAnnotation)]\n";
            $propertyCode .= '    private ' . $this->getPHPTypeFromDoctrine($doctrineType) . " \$$columnName;\n";
        }

        return $propertyCode;
    }

    private function getPHPTypeFromDoctrine(string $doctrineType): string
    {
        $mapping = [
            'integer' => 'int',
            'smallint' => 'int',
            'bigint' => 'string',
            'string' => 'string',
            'text' => 'string',
            'boolean' => 'bool',
            'decimal' => 'string',
            'float' => 'float',
            'date' => '\DateTimeInterface',
            'datetime' => '\DateTimeInterface',
            'datetimetz' => '\DateTimeInterface',
            'time' => '\DateTimeInterface',
            'array' => 'array',
            'json' => 'array',
            'object' => 'object',
            'binary' => 'string',
            'blob' => 'string',
            'guid' => 'string',
        ];

        return $mapping[$doctrineType] ?? 'mixed';
    }

    private function getPrimaryKeyColumns(string $tableName): array
    {
        $schemaManager = $this->connection->createSchemaManager();
        $indexes = $schemaManager->listTableIndexes($tableName);

        if (isset($indexes['primary'])) {
            return $indexes['primary']->getColumns();
        }

        return [];
    }

    /**
     * Generates getter and setter methods for entity properties.
     *
     * @param Column $column The database column.
     *
     * @return string The generated getter and setter methods.
     */
    private function generateGettersAndSetters(Column $column): string
    {
        $columnName = $column->getName();
        $methodName = ucfirst($columnName);

        return "
    public function get$methodName()
    {
        return \$this->$columnName;
    }

    public function set$methodName(\$value)
    {
        \$this->$columnName = \$value;
    }\n";
    }

    /**
     * Parses a relation annotation string to extract mappedBy and targetEntity values.
     *
     * @param string $relation The relation annotation string.
     *
     * @return array Associative array containing mappedBy and targetEntity values.
     */
    private function parseRelationAnnotation(string $relation): array
    {
        $pattern = '/mappedBy:\s*"([^"]+)",\s*targetEntity:\s*([^\s:]+)::class/';

        if (preg_match($pattern, $relation, $matches)) {
            return [
                'mappedBy' => $matches[1],
                'targetEntity' => $matches[2],
            ];
        }

        return [
            'mappedBy' => null,
            'targetEntity' => null,
        ];
    }
}