package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.InsertOption
import org.komapper.jdbc.DatabaseConfig

internal class EntityUpsertSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    option: InsertOption,
    private val entity: ENTITY,
) : JdbcQueryRunner<Int> {

    private val support: EntityUpsertQueryRunnerSupport<ENTITY, ID, META> = EntityUpsertQueryRunnerSupport(context, option)

    override fun run(config: DatabaseConfig): Int {
        val newEntity = preUpsert(config, entity)
        val (count) = upsert(config, newEntity)
        return count
    }

    private fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: DatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = buildStatement(config, entity)
        return support.upsert(config) { it.executeUpdate(statement) }
    }

    override fun dryRun(config: DatabaseConfig): String {
        val statement = buildStatement(config, entity)
        return statement.toSql()
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, listOf(entity))
    }
}
