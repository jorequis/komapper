package org.komapper.core.dsl.metamodel

data class Index(
    val name: String,
    val columns: List<Column<*, *, *>>,
    val type: Type = Type.BTREE
) {
    enum class Type {
        BTREE,
        HASH
    }
}