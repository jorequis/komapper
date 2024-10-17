package org.komapper.core.dsl.metamodel

data class Column<EXTERIOR : Any, INTERIOR : Any, ENTITY: Any>(
    val name: String,
    val descriptor: PropertyDescriptor<ENTITY, EXTERIOR, INTERIOR>,
    val metamodel: PropertyMetamodel<ENTITY, EXTERIOR, INTERIOR>
)
