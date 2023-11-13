package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.metamodel.*
import java.time.LocalDateTime

interface SchemaStatementBuilder {
    fun create(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement>
    fun createMissingProperties(metamodel: EntityMetamodel<*, *, *>, columns: List<String>, indexes: List<String>): List<Statement>
    fun drop(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement>
}

abstract class AbstractSchemaStatementBuilder(
    protected val dialect: BuilderDialect,
) : SchemaStatementBuilder {

    override fun create(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> {
        val statements = mutableListOf<Statement>()
        for (e in metamodels) {
            statements.addAll(createTable(e))
            statements.addAll(createSequence(e))
        }
        return statements.filter { it.parts.isNotEmpty() }
    }

    override fun drop(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> {
        val statements = mutableListOf<Statement>()
        for (e in metamodels) {
            statements.addAll(dropTable(e))
            statements.addAll(dropSequence(e))
        }
        return statements.filter { it.parts.isNotEmpty() }
    }

    protected open fun createTable(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        val tableName = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append("create table ")
        if (dialect.supportsCreateIfNotExists()) buf.append("if not exists ")
        buf.append("$tableName (")
        val columnsDefinition = metamodel.properties().joinToString { p -> columnDefinition(p) }
        buf.append(columnsDefinition)
        val primaryKeys = metamodel.idProperties() - metamodel.virtualIdProperties().toSet()
        if (primaryKeys.isNotEmpty()) buf.appendPrimaryKeysDefinition(metamodel = metamodel, primaryKeys = primaryKeys)
        val indexDefinitions = listOf(
            metamodel.foreignKeys().joinToString { foreignKey -> ", ${foreignKeyDefinition(metamodel = metamodel, foreignKey = foreignKey)}" },
            metamodel.uniqueKeys().joinToString { uniqueKey -> ", ${uniqueKeysDefinition(metamodel = metamodel, uniqueKey = uniqueKey)}" },
            metamodel.indexes().joinToString { index -> ", ${indexDefinition(metamodel = metamodel, index = index, onTable = false)}" }
        )
        buf.append(indexDefinitions.filter { it.isNotBlank() }.joinToString())
        buf.append(")")
        return listOf(buf.toStatement())
    }

    override fun createMissingProperties(metamodel: EntityMetamodel<*, *, *>, columns: List<String>, indexes: List<String>): List<Statement> {
        val buf = StatementBuffer()
        val tableName = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append("alter table $tableName ")
        val columnsDefinition = metamodel.properties().filter { !columns.contains(it.name) }.joinToString { p -> "ADD COLUMN ${columnDefinition(p)}" }

        if (columnsDefinition.isNotEmpty()) buf.append("$columnsDefinition, ")

        val indexDefinitions = listOf(
            metamodel.foreignKeys().filter { !indexes.contains(it.name) }.joinToString { foreignKey -> "ADD ${foreignKeyDefinition(metamodel = metamodel, foreignKey = foreignKey)}" },
            metamodel.uniqueKeys().filter { !indexes.contains(it.name) }.joinToString { uniqueKey -> "ADD ${uniqueKeysDefinition(metamodel = metamodel, uniqueKey = uniqueKey)}" },
            metamodel.indexes().filter { !indexes.contains(it.name) }.joinToString { index -> "ADD ${indexDefinition(metamodel = metamodel, index = index, onTable = false)}" }
        )
        buf.append(indexDefinitions.filter { it.isNotBlank() }.joinToString())
        return listOf(buf.toStatement())
    }

    protected open fun resolveDataTypeName(property: PropertyMetamodel<*, *, *>): String {
        return dialect.getDataTypeName(property.interiorClass, property.options)
    }

    protected open fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        return if (property.isAutoIncrement()) " auto_increment" else ""
    }

    private fun resolveDefaultValue(property: PropertyMetamodel<*, *, *>): String {
        return if (property.defaultValue != null) " DEFAULT ${
            when (property.defaultValue!!::class) {
                LocalDateTime::class, String::class -> "'${property.defaultValue}'"
                else -> property.defaultValue.toString()
            }
        }" else ""
    }

    protected open fun createSequence(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        val idGenerator = metamodel.idGenerator()
        if (idGenerator is IdGenerator.Sequence<*, *>) {
            buf.append("create sequence ")
            if (dialect.supportsCreateIfNotExists()) {
                buf.append("if not exists ")
            }
            buf.append("${idGenerator.getCanonicalSequenceName(dialect::enquote)} start with ${idGenerator.startWith} increment by ${idGenerator.incrementBy}")
        }
        return listOf(buf.toStatement())
    }

    protected open fun dropTable(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        buf.append("drop table ")
        if (dialect.supportsDropIfExists()) {
            buf.append("if exists ")
        }
        buf.append(metamodel.getCanonicalTableName(dialect::enquote))
        return listOf(buf.toStatement())
    }

    protected open fun dropSequence(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        val idGenerator = metamodel.idGenerator()
        if (idGenerator is IdGenerator.Sequence<*, *>) {
            buf.append("drop sequence ")
            if (dialect.supportsDropIfExists()) {
                buf.append("if exists ")
            }
            buf.append(idGenerator.getCanonicalSequenceName(dialect::enquote))
        }
        return listOf(buf.toStatement())
    }

    protected open fun extractSchemaNames(metamodels: List<EntityMetamodel<*, *, *>>): List<String> {
        val tableSchemaNames = metamodels.map { it.schemaName() }
        val sequenceSchemaNames =
            metamodels.mapNotNull { it.idGenerator() }.filterIsInstance<IdGenerator.Sequence<*, *>>()
                .map { it.schemaName }
        return (tableSchemaNames + sequenceSchemaNames).distinct().filter { it.isNotBlank() }
    }

    private fun columnDefinition(propertyMetamodel: PropertyMetamodel<*, *, *>): String {
        val columnName = propertyMetamodel.getCanonicalColumnName(dialect::enquote)
        val dataTypeName = resolveDataTypeName(propertyMetamodel)
        val notNull = if (propertyMetamodel.nullable) "" else " not null"
        val identity = resolveIdentity(propertyMetamodel)
        val defaultValue = resolveDefaultValue(propertyMetamodel)
        return "$columnName ${dataTypeName}$identity$notNull$defaultValue"
    }

    private fun StatementBuffer.appendPrimaryKeysDefinition(metamodel: EntityMetamodel<*, *, *>, primaryKeys: List<PropertyMetamodel<*, *, *>>) {
        this.append(", ")
        val primaryKeyName = "pk_${metamodel.tableName()}"
        this.append("constraint $primaryKeyName primary key(")
        val pkList = primaryKeys.joinToString { p ->
            p.getCanonicalColumnName(dialect::enquote)
        }
        this.append(pkList)
        this.append(")")
    }

    private fun foreignKeyDefinition(metamodel: EntityMetamodel<*, *, *>, foreignKey: ForeignKey): String {
        return "CONSTRAINT `${foreignKeyName(metamodel, foreignKey)}` " +
            "FOREIGN KEY (`${foreignKey.name}`) REFERENCES `${foreignKey.referenceColumn.metamodel.owner.tableName()}` (`${foreignKey.referenceColumn.name}`) " +
            "ON DELETE ${foreignKey.onDelete.sql} ON UPDATE ${foreignKey.onUpdate.sql}"
    }

    private fun uniqueKeysDefinition(metamodel: EntityMetamodel<*, *, *>, uniqueKey: UniqueKey): String {
        return "CONSTRAINT `${uniqueKeyName(metamodel, uniqueKey)}` UNIQUE (${uniqueKey.columns.joinToString { it.name }})"
    }

    private fun indexDefinition(metamodel: EntityMetamodel<*, *, *>, index: Index, onTable: Boolean = false): String {
        return "INDEX `${indexName(metamodel, index)}`${if (onTable) " ON ${metamodel.tableName()} " else " "}(${index.columns.joinToString { it.name }}) USING ${index.type}"
    }

    private fun foreignKeyName(metamodel: EntityMetamodel<*, *, *>, foreignKey: ForeignKey) = foreignKey.name//"fk_${metamodel.tableName()}_${foreignKey.name}_${foreignKey.referenceColumn.name}"
    private fun uniqueKeyName(metamodel: EntityMetamodel<*, *, *>, uniqueKey: UniqueKey) = uniqueKey.name//"uk_${metamodel.tableName()}_${uniqueKey.name}"
    private fun indexName(metamodel: EntityMetamodel<*, *, *>, index: Index) = index.name//"idx_${metamodel.tableName()}_${index.name}"
}

object DryRunSchemaStatementBuilder : SchemaStatementBuilder {
    override fun create(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> = emptyList()
    override fun createMissingProperties(metamodel: EntityMetamodel<*, *, *>, columns: List<String>, indexes: List<String>): List<Statement> = emptyList()
    override fun drop(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> = emptyList()
}
