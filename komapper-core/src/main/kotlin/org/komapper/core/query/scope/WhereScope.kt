package org.komapper.core.query.scope

import org.komapper.core.Scope

@Scope
class WhereScope internal constructor(
    private val support: FilterScopeSupport
) : FilterScope by support {

    companion object {
        operator fun WhereDeclaration.plus(other: WhereDeclaration): WhereDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }
}
