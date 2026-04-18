package com.github.hirofumi.termback.session

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@JvmInline
value class TermbackSessionId(
    val value: String,
) {
    override fun toString(): String = value

    companion object {
        fun generate(): TermbackSessionId = TermbackSessionId(UUID.randomUUID().toString())
    }
}
