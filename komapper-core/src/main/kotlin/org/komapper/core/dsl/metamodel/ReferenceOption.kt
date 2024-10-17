package org.komapper.core.dsl.metamodel

enum class ReferenceOption(val sql: String) {
    RESTRICT("RESTRICT"),
    NO_ACTION("NO ACTION"),
    CASCADE("CASCADE"),
    SET_NULL("SET NULL"),
    SET_DEFAULT("SET DEFAULT")
}
