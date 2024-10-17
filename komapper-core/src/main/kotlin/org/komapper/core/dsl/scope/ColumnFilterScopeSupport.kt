package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.CompositeColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.SqlBuilderScope
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.Column
import org.komapper.core.dsl.metamodel.ColumnPair
import org.komapper.core.dsl.operator.CriteriaContext
import java.util.Deque
import java.util.LinkedList

open class ColumnFilterScopeSupport<F : ColumnFilterScope<F>>(
    private val constructFilterScope: (ColumnFilterScopeSupport<F>) -> F,
    private val deque: Deque<MutableList<Criterion>> = LinkedList(),
    private val criteria: MutableList<Criterion> = mutableListOf(),
) : ColumnFilterScope<F> {

    fun toList(): List<Criterion> {
        return criteria.toList()
    }

    internal fun add(criterion: Criterion) {
        criteria.add(criterion)
    }

    private fun <T : Any, S : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: Column<T, S, *>,
        right: Column<T, S, *>,
    ) {
        criteria.add(operator(Operand.Column(left.metamodel), Operand.Column(right.metamodel)))
    }

    private fun <T : Any, S : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: Column<T, S, *>,
        right: T,
    ) {
        criteria.add(operator(Operand.Column(left.metamodel), Operand.Argument(left.metamodel, right)))
    }

    private fun <T : Any, S : Any> add(
        operator: (Operand, Operand) -> Criterion,
        left: T,
        right: Column<T, S, *>,
    ) {
        criteria.add(operator(Operand.Argument(right.metamodel, left), Operand.Column(right.metamodel)))
    }

    private fun <T : Any, S : CharSequence> addLikeOperator(left: Column<T, S, *>, right: EscapeExpression) {
        criteria.add(Criterion.Like(Operand.Column(left.metamodel), Operand.Escape(left.metamodel, right)))
    }

    private fun <T : Any, S : CharSequence> addNotLikeOperator(left: Column<T, S, *>, right: EscapeExpression) {
        criteria.add(Criterion.NotLike(Operand.Column(left.metamodel), Operand.Escape(left.metamodel, right)))
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.eq(operand: Column<T, S, *>) {
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.eq(operand: T?) {
        if (operand == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.eq(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.notEq(operand: Column<T, S, *>) {
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.notEq(operand: T?) {
        if (operand == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.notEq(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.less(operand: Column<T, S, *>) {
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.less(operand: T?) {
        if (operand == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.less(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.lessEq(operand: Column<T, S, *>) {
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.lessEq(operand: T?) {
        if (operand == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.lessEq(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.greater(operand: Column<T, S, *>) {
        add(Criterion::Greater, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.greater(operand: T?) {
        if (operand == null) return
        add(Criterion::Greater, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.greater(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::Greater, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.greaterEq(operand: Column<T, S, *>) {
        add(Criterion::GreaterEq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.greaterEq(operand: T?) {
        if (operand == null) return
        add(Criterion::GreaterEq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.greaterEq(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::GreaterEq, this, operand)
    }

    override fun <T : Any, S : Any> Column<T, S, *>.isNull() {
        val left = Operand.Column(this.metamodel)
        add(Criterion.IsNull(left))
    }

    override fun <T : Any, S : Any> Column<T, S, *>.isNotNull() {
        val left = Operand.Column(this.metamodel)
        add(Criterion.IsNotNull(left))
    }

    override infix fun <T : Any, S : CharSequence> Column<T, S, *>.like(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, text(operand))
    }

    override infix fun <T : Any, S : CharSequence> Column<T, S, *>.notLike(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, text(operand))
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.startsWith(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asPrefix())
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.notStartsWith(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asPrefix())
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.contains(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asInfix())
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.notContains(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asInfix())
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.endsWith(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asSuffix())
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.notEndsWith(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asSuffix())
    }

    override infix fun <T : Comparable<T>, S : Any> Column<T, S, *>.between(range: ClosedRange<T>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Argument(this.metamodel, range.start) to Operand.Argument(this.metamodel, range.endInclusive)
        add(Criterion.Between(left, right))
    }

    override infix fun <T : Comparable<T>, S : Any> Column<T, S, *>.notBetween(range: ClosedRange<T>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Argument(this.metamodel, range.start) to Operand.Argument(this.metamodel, range.endInclusive)
        add(Criterion.NotBetween(left, right))
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.inList(values: List<T?>) {
        val o1 = Operand.Column(this.metamodel)
        val o2 = values.map { Operand.Argument(this.metamodel, it) }
        add(Criterion.InList(o1, o2))
    }

    override fun <T : Any, S : Any> Column<T, S, *>.inList(subquery: SubqueryExpression<T?>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Subquery(subquery)
        add(Criterion.InSubQuery(left, right))
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.inList(block: () -> SubqueryExpression<T?>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Subquery(block())
        add(Criterion.InSubQuery(left, right))
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.notInList(values: List<T?>) {
        val o1 = Operand.Column(this.metamodel)
        val o2 = values.map { Operand.Argument(this.metamodel, it) }
        add(Criterion.NotInList(o1, o2))
    }

    override fun <T : Any, S : Any> Column<T, S, *>.notInList(subquery: SubqueryExpression<T?>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Subquery(subquery)
        add(Criterion.NotInSubQuery(left, right))
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.notInList(block: () -> SubqueryExpression<T?>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Subquery(block())
        add(Criterion.NotInSubQuery(left, right))
    }

    override infix fun <A : Any, B : Any> ColumnPair<A, B>.inList2(values: List<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = values.map {
            Operand.Argument(this.first.metamodel, it.first) to Operand.Argument(
                this.second.metamodel,
                it.second,
            )
        }
        add(Criterion.InList2(left, right))
    }

    override fun <A : Any, B : Any> ColumnPair<A, B>.inList2(subquery: SubqueryExpression<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = Operand.Subquery(subquery)
        add(Criterion.InSubQuery2(left, right))
    }

    override infix fun <A : Any, B : Any> ColumnPair<A, B>.inList2(block: () -> SubqueryExpression<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = Operand.Subquery(block())
        add(Criterion.InSubQuery2(left, right))
    }

    override infix fun <A : Any, B : Any> ColumnPair<A, B>.notInList2(values: List<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = values.map {
            Operand.Argument(this.first.metamodel, it.first) to Operand.Argument(
                this.second.metamodel,
                it.second,
            )
        }
        add(Criterion.NotInList2(left, right))
    }

    override fun <A : Any, B : Any> ColumnPair<A, B>.notInList2(subquery: SubqueryExpression<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = Operand.Subquery(subquery)
        add(Criterion.NotInSubQuery2(left, right))
    }

    override infix fun <A : Any, B : Any> ColumnPair<A, B>.notInList2(block: () -> SubqueryExpression<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = Operand.Subquery(block())
        add(Criterion.NotInSubQuery2(left, right))
    }
}
