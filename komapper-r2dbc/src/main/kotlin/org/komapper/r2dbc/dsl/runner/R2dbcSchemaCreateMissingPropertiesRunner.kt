package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.runner.SchemaCreateMissingPropertiesRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

class R2dbcSchemaCreateMissingPropertiesRunner(
    metamodel: EntityMetamodel<*, *, *>,
    columns: List<String>,
    indexes: List<String>
) : R2dbcRunner<Unit> {

    private val runner = SchemaCreateMissingPropertiesRunner(metamodel, columns, indexes)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statements = runner.buildStatements(config)
        val executor = R2dbcExecutor(config, SchemaOptions.DEFAULT)
        executor.execute(statements)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
