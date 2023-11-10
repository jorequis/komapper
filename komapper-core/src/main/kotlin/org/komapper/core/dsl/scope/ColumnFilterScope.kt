package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.*
import org.komapper.core.dsl.metamodel.Column
import org.komapper.core.dsl.metamodel.ColumnPair
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
    infix fun <T : Any, S : Any> Column<T, S, *>.eq(operand: Column<T, S, *>)

    /**
     * Applies the `=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.eq(operand: T?)

    /**
     * Applies the `=` operator.
     */
    infix fun <T : Any, S : Any> T?.eq(operand: Column<T, S, *>)

    /**
     * Applies the `<>` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.notEq(operand: Column<T, S, *>)

    /**
     * Applies the `<>` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.notEq(operand: T?)

    /**
     * Applies the `<>` operator.
     */
    infix fun <T : Any, S : Any> T?.notEq(operand: Column<T, S, *>)

    /**
     * Applies the `<` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.less(operand: Column<T, S, *>)

    /**
     * Applies the `<` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.less(operand: T?)

    /**
     * Applies the `<` operator.
     */
    infix fun <T : Any, S : Any> T?.less(operand: Column<T, S, *>)

    /**
     * Applies the `<=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.lessEq(operand: Column<T, S, *>)

    /**
     * Applies the `<=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.lessEq(operand: T?)

    /**
     * Applies the `<=` operator.
     */
    infix fun <T : Any, S : Any> T?.lessEq(operand: Column<T, S, *>)

    /**
     * Applies the `>` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.greater(operand: Column<T, S, *>)

    /**
     * Applies the `>` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.greater(operand: T?)

    /**
     * Applies the `>` operator.
     */
    infix fun <T : Any, S : Any> T?.greater(operand: Column<T, S, *>)

    /**
     * Applies the `>=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.greaterEq(operand: Column<T, S, *>)

    /**
     * Applies the `>=` operator.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.greaterEq(operand: T?)

    /**
     * Applies the `>=` operator.
     */
    infix fun <T : Any, S : Any> T?.greaterEq(operand: Column<T, S, *>)

    /**
     * Applies the `IS NULL` predicate.
     */
    fun <T : Any, S : Any> Column<T, S, *>.isNull()

    /**
     * Applies the `IS NOT NULL` predicate.
     */
    fun <T : Any, S : Any> Column<T, S, *>.isNotNull()

    /**
     * Applies the `LIKE` predicate.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.like(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.notLike(operand: CharSequence?)

    /**
     * Applies the `LIKE` predicate.
     * It is translated to `LIKE operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.startsWith(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     * It is translated to `NOT LIKE operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.notStartsWith(operand: CharSequence?)

    /**
     * Applies the `LIKE` predicate.
     * It is translated to `LIKE '%' + operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.contains(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     * It is translated to `NOT LIKE '%' + operand + '%'`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.notContains(operand: CharSequence?)

    /**
     * Applies the `LIKE` predicate.
     * It is translated to `LIKE '%' + operand`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.endsWith(operand: CharSequence?)

    /**
     * Applies the `NOT LIKE` predicate.
     * It is translated to `NOT LIKE '%' + operand`.
     */
    infix fun <T : Any, S : CharSequence> Column<T, S, *>.notEndsWith(operand: CharSequence?)

    /**
     * Applies the `BETWEEN` predicate.
     */
    infix fun <T : Comparable<T>, S : Any> Column<T, S, *>.between(range: ClosedRange<T>)

    /**
     * Applies the `NOT BETWEEN` predicate.
     */
    infix fun <T : Comparable<T>, S : Any> Column<T, S, *>.notBetween(range: ClosedRange<T>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.inList(values: List<T?>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.inList(subquery: SubqueryExpression<T?>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.inList(block: () -> SubqueryExpression<T?>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.notInList(values: List<T?>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.notInList(subquery: SubqueryExpression<T?>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <T : Any, S : Any> Column<T, S, *>.notInList(block: () -> SubqueryExpression<T?>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <A : Any, B : Any> ColumnPair<A, B>.inList2(values: List<Pair<A?, B?>>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <A : Any, B : Any> ColumnPair<A, B>.inList2(subquery: SubqueryExpression<Pair<A?, B?>>)

    /**
     * Applies the `IN` predicate.
     */
    infix fun <A : Any, B : Any> ColumnPair<A, B>.inList2(block: () -> SubqueryExpression<Pair<A?, B?>>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <A : Any, B : Any> ColumnPair<A, B>.notInList2(values: List<Pair<A?, B?>>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <A : Any, B : Any> ColumnPair<A, B>.notInList2(subquery: SubqueryExpression<Pair<A?, B?>>)

    /**
     * Applies the `NOT IN` predicate.
     */
    infix fun <A : Any, B : Any> ColumnPair<A, B>.notInList2(block: () -> SubqueryExpression<Pair<A?, B?>>)

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
