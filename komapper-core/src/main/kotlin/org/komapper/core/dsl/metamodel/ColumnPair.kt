package org.komapper.core.dsl.metamodel

data class ColumnPair<A : Any, B : Any> (val first: Column<A, *, *>, val second: Column<B, *, *>)
