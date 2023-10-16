package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.CompositeColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.SqlBuilderScope
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.Column
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

    override infix fun <T : Any, S : Any> Column<T, S, *>.eq1(operand: Column<T, S, *>) {
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.eq1(operand: T?) {
        if (operand == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.eq1(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.notEq1(operand: Column<T, S, *>) {
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.notEq1(operand: T?) {
        if (operand == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.notEq1(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.less1(operand: Column<T, S, *>) {
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.less1(operand: T?) {
        if (operand == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.less1(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.lessEq1(operand: Column<T, S, *>) {
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.lessEq1(operand: T?) {
        if (operand == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.lessEq1(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.greater1(operand: Column<T, S, *>) {
        add(Criterion::Greater, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.greater1(operand: T?) {
        if (operand == null) return
        add(Criterion::Greater, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.greater1(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::Greater, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.greaterEq1(operand: Column<T, S, *>) {
        add(Criterion::GreaterEq, this, operand)
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.greaterEq1(operand: T?) {
        if (operand == null) return
        add(Criterion::GreaterEq, this, operand)
    }

    override infix fun <T : Any, S : Any> T?.greaterEq1(operand: Column<T, S, *>) {
        if (this == null) return
        add(Criterion::GreaterEq, this, operand)
    }

    override fun <T : Any, S : Any> Column<T, S, *>.isNull1() {
        val left = Operand.Column(this.metamodel)
        add(Criterion.IsNull(left))
    }

    override fun <T : Any, S : Any> Column<T, S, *>.isNotNull1() {
        val left = Operand.Column(this.metamodel)
        add(Criterion.IsNotNull(left))
    }

    override infix fun <T : Any, S : CharSequence> Column<T, S, *>.like1(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, text(operand))
    }

    override infix fun <T : Any, S : CharSequence> Column<T, S, *>.notLike1(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, text(operand))
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.startsWith1(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asPrefix())
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.notStartsWith1(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asPrefix())
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.contains1(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asInfix())
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.notContains1(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asInfix())
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.endsWith1(operand: CharSequence?) {
        if (operand == null) return
        addLikeOperator(this, operand.asSuffix())
    }

    override fun <T : Any, S : CharSequence> Column<T, S, *>.notEndsWith1(operand: CharSequence?) {
        if (operand == null) return
        addNotLikeOperator(this, operand.asSuffix())
    }

    override infix fun <T : Comparable<T>, S : Any> Column<T, S, *>.between1(range: ClosedRange<T>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Argument(this.metamodel, range.start) to Operand.Argument(this.metamodel, range.endInclusive)
        add(Criterion.Between(left, right))
    }

    override infix fun <T : Comparable<T>, S : Any> Column<T, S, *>.notBetween1(range: ClosedRange<T>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Argument(this.metamodel, range.start) to Operand.Argument(this.metamodel, range.endInclusive)
        add(Criterion.NotBetween(left, right))
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.inList1(values: List<T?>) {
        val o1 = Operand.Column(this.metamodel)
        val o2 = values.map { Operand.Argument(this.metamodel, it) }
        add(Criterion.InList(o1, o2))
    }

    override fun <T : Any, S : Any> Column<T, S, *>.inList1(subquery: SubqueryExpression<T?>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Subquery(subquery)
        add(Criterion.InSubQuery(left, right))
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.inList1(block: () -> SubqueryExpression<T?>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Subquery(block())
        add(Criterion.InSubQuery(left, right))
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.notInList1(values: List<T?>) {
        val o1 = Operand.Column(this.metamodel)
        val o2 = values.map { Operand.Argument(this.metamodel, it) }
        add(Criterion.NotInList(o1, o2))
    }

    override fun <T : Any, S : Any> Column<T, S, *>.notInList1(subquery: SubqueryExpression<T?>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Subquery(subquery)
        add(Criterion.NotInSubQuery(left, right))
    }

    override infix fun <T : Any, S : Any> Column<T, S, *>.notInList1(block: () -> SubqueryExpression<T?>) {
        val left = Operand.Column(this.metamodel)
        val right = Operand.Subquery(block())
        add(Criterion.NotInSubQuery(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.inList21(values: List<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = values.map {
            Operand.Argument(this.first.metamodel, it.first) to Operand.Argument(
                this.second.metamodel,
                it.second,
            )
        }
        add(Criterion.InList2(left, right))
    }

    override fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.inList21(subquery: SubqueryExpression<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = Operand.Subquery(subquery)
        add(Criterion.InSubQuery2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.inList21(block: () -> SubqueryExpression<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = Operand.Subquery(block())
        add(Criterion.InSubQuery2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.notInList21(values: List<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = values.map {
            Operand.Argument(this.first.metamodel, it.first) to Operand.Argument(
                this.second.metamodel,
                it.second,
            )
        }
        add(Criterion.NotInList2(left, right))
    }

    override fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.notInList21(subquery: SubqueryExpression<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = Operand.Subquery(subquery)
        add(Criterion.NotInSubQuery2(left, right))
    }

    override infix fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.notInList21(block: () -> SubqueryExpression<Pair<A?, B?>>) {
        val left = Operand.Column(this.first.metamodel) to Operand.Column(this.second.metamodel)
        val right = Operand.Subquery(block())
        add(Criterion.NotInSubQuery2(left, right))
    }
}
