package org.komapper.core.dsl.metamodel

data class Join<COLUMN_TYPE : Any>(
    val table: Table<*>,
    val columnA: Column<COLUMN_TYPE, COLUMN_TYPE, *>,
    val columnB: Column<COLUMN_TYPE, COLUMN_TYPE, *>
)
