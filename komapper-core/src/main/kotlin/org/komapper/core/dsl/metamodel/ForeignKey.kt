package org.komapper.core.dsl.metamodel

data class ForeignKey(
    val name: String,
    val referenceColumn: Column<*, *, *>,
    val onDelete: ReferenceOption,
    val onUpdate: ReferenceOption
)
