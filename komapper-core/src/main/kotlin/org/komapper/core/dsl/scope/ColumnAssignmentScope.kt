package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.Column
import org.komapper.core.dsl.metamodel.PropertyMetamodel

open class ColumnAssignmentScope<ENTITY : Any>(
    private val assignments: MutableList<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> = mutableListOf()
) {

    /**
     * The `=` operator.
     */
    infix fun <T : Any> Column<T, *, ENTITY>.eq(value: T?) {
        val right = Operand.Argument(this.metamodel, value)
        assignments.add(this.metamodel to right)
    }

    /**
     * The `=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, *, ENTITY>.eq(operand: ColumnExpression<T, S>) {
        val right = Operand.Column(operand)
        assignments.add(this.metamodel to right)
    }

    /**
     * Behaves like the `=` operator only if the value is not `null`.
     * If the value is `null`, the assignment is ignored.
     */
    infix fun <T : Any, S : Any> Column<T, *, ENTITY>.eqIfNotNull(value: T?) {
        if (value == null) return
        val right = Operand.Argument(this.metamodel, value)
        assignments.add(this.metamodel to right)
    }
}
