package org.komapper.core.dsl.builder

import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.data.StatementBuffer
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationComponent
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.expr.IndexedSortItem

internal class SqlSetOperationStatementBuilder(
    private val dialect: Dialect,
    private val context: SqlSetOperationContext<*>
) {

    private val buf = StatementBuffer(dialect::formatValue)

    fun build(): Statement {
        visitSetOperationComponent(context.component)
        if (context.orderBy.isNotEmpty()) {
            buf.append(" order by ")
            for (item in context.orderBy) {
                val (index, sort) = when (item) {
                    is IndexedSortItem.Asc -> item.index to "asc"
                    is IndexedSortItem.Desc -> item.index to "desc"
                }
                buf.append("$index $sort, ")
            }
            buf.cutBack(2)
        }
        return buf.toStatement()
    }

    private fun visitSetOperationComponent(component: SqlSetOperationComponent<*>) {
        when (component) {
            is SqlSetOperationComponent.Leaf -> visitSelectContext(component.context)
            is SqlSetOperationComponent.Composite -> {
                visitSetOperationComponent(component.left)
                val operator = when (component.kind) {
                    SqlSetOperationKind.INTERSECT -> "intersect"
                    SqlSetOperationKind.EXCEPT -> "except"
                    SqlSetOperationKind.UNION -> "union"
                    SqlSetOperationKind.UNION_ALL -> "union all"
                }
                buf.append(" $operator ")
                visitSetOperationComponent(component.right)
            }
        }
    }

    private fun visitSelectContext(selectContext: SqlSelectContext<*>) {
        val builder = SqlSelectStatementBuilder(dialect, selectContext)
        val statement = builder.build()
        buf.append(statement)
    }
}
