package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to create missing tables properties and their associated constraints.
 * This query returns Unit.
 */
interface SchemaCreateMissingPropertiesQuery : Query<Unit> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaCreateMissingPropertiesQuery
}

internal data class SchemaCreateMissingPropertiesQueryImpl(
    private val metamodel: EntityMetamodel<*, *, *>,
    private val columns: List<String>,
    private val indexes: List<String>
) : SchemaCreateMissingPropertiesQuery {

    override fun options(configure: (SchemaOptions) -> SchemaOptions): SchemaCreateMissingPropertiesQuery {
        return this
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.schemaCreateMissingPropertiesQuery(metamodel, columns, indexes)
    }
}
