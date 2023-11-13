package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel

class SchemaCreateMissingPropertiesRunner(
    private val metamodel: EntityMetamodel<*, *, *>,
    private val columns: List<String>,
    private val indexes: List<String>
) : Runner {

    override fun check(config: DatabaseConfig) = Unit

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val statements = buildStatements(config)
        return DryRunStatement.of(statements, config)
    }

    fun buildStatements(config: DatabaseConfig): List<Statement> {
        val builder = config.dialect.getSchemaStatementBuilder(BuilderDialect(config))
        return builder.createMissingProperties(metamodel, columns, indexes)
    }
}
