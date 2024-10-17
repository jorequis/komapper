package org.komapper.core.dsl.metamodel

data class UniqueKey(
    val name: String,
    val columns: List<Column<*, *, *>>,
)
