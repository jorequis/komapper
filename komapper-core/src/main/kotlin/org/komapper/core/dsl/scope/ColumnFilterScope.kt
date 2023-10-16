package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.*
import org.komapper.core.dsl.metamodel.Column
import org.komapper.core.dsl.operator.asInfix as asInfixFunction
import org.komapper.core.dsl.operator.asPrefix as asPrefixFunction
import org.komapper.core.dsl.operator.asSuffix as asSuffixFunction

/**
 * Provides operators and predicates for HAVING, ON, WHEN, and WHERE clauses.
 */
interface ColumnFilterScope<F : ColumnFilterScope<F>> {
    /**
     * Applies the `=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.eq1(operand: Column<T, S, *>)

    /**
     * Applies the `=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.eq1(operand: T?)

    /**
     * Applies the `=` operator.
     */
    infix fun <T : Any, S : Any> T?.eq1(operand: Column<T, S, *>)

    /**
     * Applies the `<>` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.notEq1(operand: Column<T, S, *>)

    /**
     * Applies the `<>` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.notEq1(operand: T?)

    /**
     * Applies the `<>` operator.
     */
    infix fun <T : Any, S : Any> T?.notEq1(operand: Column<T, S, *>)

    /**
     * Applies the `<` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.less1(operand: Column<T, S, *>)

    /**
     * Applies the `<` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.less1(operand: T?)

    /**
     * Applies the `<` operator.
     */
    infix fun <T : Any, S : Any> T?.less1(operand: Column<T, S, *>)

    /**
     * Applies the `<=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.lessEq1(operand: Column<T, S, *>)

    /**
     * Applies the `<=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.lessEq1(operand: T?)

    /**
     * Applies the `<=` operator.
     */
    infix fun <T : Any, S : Any> T?.lessEq1(operand: Column<T, S, *>)

    /**
     * Applies the `>` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.greater1(operand: Column<T, S, *>)

    /**
     * Applies the `>` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.greater1(operand: T?)

    /**
     * Applies the `>` operator.
     */
    infix fun <T : Any, S : Any> T?.greater1(operand: Column<T, S, *>)

    /**
     * Applies the `>=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.greaterEq1(operand: Column<T, S, *>)

    /**
     * Applies the `>=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.greaterEq1(operand: T?)

    /**
     * Applies the `>=` operator.
     */
    infix fun <T : Any, S : Any> T?.greaterEq1(operand: Column<T, S, *>)

    /**
     * Applies the `IS NULL` predicate.
     */
    fun <T : Any, S : Any> Column<T, S, *>.isNull1()

    /**
     * Applies the `IS NOT NULL` predicate.
     */
    fun <T : Any, S : Any> Column<T, S, *>.isNotNull1()

    /**
     * Applies the `LIKE` predicate.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.like1(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.notLike1(operand: CharSequence?)

    /**
     * Applies the `LIKE` predicate.
     * It is translated to `LIKE operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.startsWith1(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     * It is translated to `NOT LIKE operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.notStartsWith1(operand: CharSequence?)

    /**
     * Applies the `LIKE` predicate.
     * It is translated to `LIKE '%' + operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.contains1(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     * It is translated to `NOT LIKE '%' + operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.notContains1(operand: CharSequence?)

    /**
     * Applies the `LIKE` predicate.
     * It is translated to `LIKE '%' + operand`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.endsWith1(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     * It is translated to `NOT LIKE '%' + operand`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.notEndsWith1(operand: CharSequence?)

    /**
     * Applies the `BETWEEN` predicate.
     */
    infix fun <T : Comparable<T>, S : Any> Column<T, S, *>.between1(range: ClosedRange<T>)

    /**
     * Applies the `NOT BETWEEN` predicate.
     */
    infix fun <T : Comparable<T>, S : Any> Column<T, S, *>.notBetween1(range: ClosedRange<T>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.inList1(values: List<T?>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.inList1(subquery: SubqueryExpression<T?>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.inList1(block: () -> SubqueryExpression<T?>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.notInList1(values: List<T?>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.notInList1(subquery: SubqueryExpression<T?>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.notInList1(block: () -> SubqueryExpression<T?>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.inList21(values: List<Pair<A?, B?>>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.inList21(subquery: SubqueryExpression<Pair<A?, B?>>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.inList21(block: () -> SubqueryExpression<Pair<A?, B?>>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.notInList21(values: List<Pair<A?, B?>>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.notInList21(subquery: SubqueryExpression<Pair<A?, B?>>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <A : Any, B : Any> Pair<Column<A, *, *>, Column<B, *, *>>.notInList21(block: () -> SubqueryExpression<Pair<A?, B?>>)

    /**
     * Does not escape the given string.
     */
    fun <S : CharSequence> text(value: S): EscapeExpression = org.komapper.core.dsl.operator.text(value)

    /**
     * Escapes the given string and appends a wildcard character at the end.
     */
    fun CharSequence.asPrefix(): EscapeExpression = this.asPrefixFunction()

    /**
     * Escapes the given string and encloses it with wildcard characters.
     */
    fun CharSequence.asInfix(): EscapeExpression = this.asInfixFunction()

    /**
     * Escapes the given string and appends a wildcard character at the beginning.
     */
    fun CharSequence.asSuffix(): EscapeExpression = this.asSuffixFunction()

}
