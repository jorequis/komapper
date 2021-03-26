package org.komapper.core.query.scope

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.EntitySelectQueryImpl
import org.komapper.core.query.EntitySelectSubQuery
import org.komapper.core.query.SingleProjection
import org.komapper.core.query.context.EntitySelectContext
import org.komapper.core.query.context.FilterContext
import org.komapper.core.query.data.Criterion
import org.komapper.core.query.data.Operand
import org.komapper.core.query.option.LikeOption

internal class FilterScopeSupport(private val context: FilterContext) : FilterScope {

    override infix fun <T : Any> ColumnInfo<T>.eq(operand: ColumnInfo<T>) {
        context.add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.eq(operand: T?) {
        if (operand == null) return
        context.add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> T?.eq(operand: ColumnInfo<T>) {
        if (this == null) return
        context.add(Criterion::Eq, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.notEq(operand: ColumnInfo<T>) {
        context.add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.notEq(operand: T?) {
        if (operand == null) return
        context.add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> T?.notEq(operand: ColumnInfo<T>) {
        if (this == null) return
        context.add(Criterion::NotEq, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.less(operand: ColumnInfo<T>) {
        context.add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.less(operand: T?) {
        if (operand == null) return
        context.add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> T?.less(operand: ColumnInfo<T>) {
        if (this == null) return
        context.add(Criterion::Less, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.lessEq(operand: ColumnInfo<T>) {
        context.add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.lessEq(operand: T?) {
        if (operand == null) return
        context.add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> T?.lessEq(operand: ColumnInfo<T>) {
        if (this == null) return
        context.add(Criterion::LessEq, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.greater(operand: ColumnInfo<T>) {
        context.add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.greater(operand: T?) {
        if (operand == null) return
        context.add(Criterion::Grater, this, operand)
    }

    override infix fun <T : Any> T?.greater(operand: ColumnInfo<T>) {
        if (this == null) return
        context.add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.greaterEq(operand: ColumnInfo<T>) {
        context.add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> ColumnInfo<T>.greaterEq(operand: T?) {
        if (operand == null) return
        context.add(Criterion::GraterEq, this, operand)
    }

    override infix fun <T : Any> T?.greaterEq(operand: ColumnInfo<T>) {
        if (this == null) return
        context.add(Criterion::GraterEq, this, operand)
    }

    override fun <T : Any> ColumnInfo<T>.isNull() {
        val left = Operand.Column(this)
        context.add(Criterion.IsNull(left))
    }

    override fun <T : Any> ColumnInfo<T>.isNotNull() {
        val left = Operand.Column(this)
        context.add(Criterion.IsNotNull(left))
    }

    override infix fun <T : CharSequence> ColumnInfo<T>.like(operand: Any?) {
        if (operand == null) return
        context.add(Criterion::Like, this, operand, LikeOption.None)
    }

    override infix fun <T : CharSequence> ColumnInfo<T>.like(operand: LikeOperand) {
        val value = operand.value ?: return
        val option = createLikeOption(operand)
        context.add(Criterion::Like, this, value, option)
    }

    override infix fun <T : CharSequence> ColumnInfo<T>.notLike(operand: Any?) {
        if (operand == null) return
        context.add(Criterion::NotLike, this, operand, LikeOption.None)
    }

    override infix fun <T : CharSequence> ColumnInfo<T>.notLike(operand: LikeOperand) {
        val value = operand.value ?: return
        val option = createLikeOption(operand)
        context.add(Criterion::NotLike, this, value, option)
    }

    private fun createLikeOption(operand: LikeOperand): LikeOption {
        return when (operand) {
            is LikeOperand.Normal -> LikeOption.None
            is LikeOperand.Escape -> LikeOption.Escape(operand.escapeChar)
            is LikeOperand.Prefix -> LikeOption.Prefix(operand.escapeChar)
            is LikeOperand.Infix -> LikeOption.Infix(operand.escapeChar)
            is LikeOperand.Suffix -> LikeOption.Suffix(operand.escapeChar)
        }
    }

    override infix fun <T : Comparable<T>> ColumnInfo<T>.between(range: ClosedRange<T>) {
        val left = Operand.Column(this)
        val right = Operand.Parameter(this, range.start) to Operand.Parameter(this, range.endInclusive)
        context.add(Criterion.Between(left, right))
    }

    override infix fun <T : Comparable<T>> ColumnInfo<T>.notBetween(range: ClosedRange<T>) {
        val left = Operand.Column(this)
        val right = Operand.Parameter(this, range.start) to Operand.Parameter(this, range.endInclusive)
        context.add(Criterion.NotBetween(left, right))
    }

    override infix fun <T : Any> ColumnInfo<T>.inList(values: List<T?>) {
        val o1 = Operand.Column(this)
        val o2 = values.map { Operand.Parameter(this, it) }
        context.add(Criterion.InList(o1, o2))
    }

    override infix fun <T : Any> ColumnInfo<T>.inList(block: () -> SingleProjection) {
        this.inList(block())
    }

    override infix fun <T : Any> ColumnInfo<T>.inList(projection: SingleProjection) {
        val left = Operand.Column(this)
        val right = when (projection) {
            is SingleProjection.ContextHolder -> projection.context
            else -> TODO()
        }
        context.add(Criterion.InSubQuery(left, right))
    }

    override infix fun <T : Any> ColumnInfo<T>.notInList(values: List<T?>) {
        val o1 = Operand.Column(this)
        val o2 = values.map { Operand.Parameter(this, it) }
        context.add(Criterion.NotInList(o1, o2))
    }

    override infix fun <T : Any> ColumnInfo<T>.notInList(block: () -> SingleProjection) {
        this.notInList(block())
    }

    override infix fun <T : Any> ColumnInfo<T>.notInList(projection: SingleProjection) {
        val left = Operand.Column(this)
        val right = when (projection) {
            is SingleProjection.ContextHolder -> projection.context
            else -> TODO()
        }
        context.add(Criterion.NotInSubQuery(left, right))
    }

    override fun <ENTITY> exists(entityMetamodel: EntityMetamodel<ENTITY>): EntitySelectSubQuery<ENTITY> {
        val subContext = EntitySelectContext(entityMetamodel)
        context.add(Criterion.Exists(subContext))
        return EntitySelectQueryImpl(entityMetamodel, subContext)
    }

    override fun <ENTITY> notExists(entityMetamodel: EntityMetamodel<ENTITY>): EntitySelectSubQuery<ENTITY> {
        val subContext = EntitySelectContext(entityMetamodel)
        context.add(Criterion.NotExists(subContext))
        return EntitySelectQueryImpl(entityMetamodel, subContext)
    }

    override fun and(declaration: WhereDeclaration) {
        addCriteria(declaration, Criterion::And)
    }

    override fun or(declaration: WhereDeclaration) {
        addCriteria(declaration, Criterion::Or)
    }

    override fun not(declaration: WhereDeclaration) {
        addCriteria(declaration, Criterion::Not)
    }

    private fun addCriteria(declaration: WhereDeclaration, operator: (List<Criterion>) -> Criterion) {
        val criteria = mutableListOf<Criterion>()
        val subContext = FilterContext(criteria)
        val support = FilterScopeSupport(subContext)
        val scope = WhereScope(support)
        declaration(scope)
        context.add(operator(criteria))
    }

    override fun <T : CharSequence> T?.escape(): LikeOperand {
        return LikeOperand.Escape(this)
    }

    override fun <T : CharSequence> T?.asPrefix(): LikeOperand {
        return LikeOperand.Prefix(this)
    }

    override fun <T : CharSequence> T?.asInfix(): LikeOperand {
        return LikeOperand.Infix(this)
    }

    override fun <T : CharSequence> T?.asSuffix(): LikeOperand {
        return LikeOperand.Suffix(this)
    }
}
