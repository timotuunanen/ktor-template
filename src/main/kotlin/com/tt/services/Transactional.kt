package com.tt.services

import org.jooq.Configuration
import org.jooq.DSLContext

open class Transactional(val dsl: DSLContext) {

    fun inTx(action: DSLContext.() -> Unit) {
        dsl.transaction { trx: Configuration ->
            trx.dsl().action()
        }
    }
}
