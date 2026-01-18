package com.github.hirofumi.termback.notification

import com.github.hirofumi.termback.TermbackSessionId
import com.github.hirofumi.termback.TermbackTabState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TermbackNotificationRequest(
    val sessionId: TermbackSessionId,
    val message: String,
    val title: String? = null,
    val suppress: Suppress = Suppress.WHEN_ACTIVE,
    val onNext: OnNext = OnNext.EXPIRE,
) {
    /**
     * Controls notification suppression based on tab state.
     * When the condition is met:
     * - At creation time: notification is skipped (not created)
     * - After creation: notification is expired (dismissed)
     */
    @Serializable
    enum class Suppress {
        /** Never suppress. */
        @SerialName("none")
        NONE,

        /** Suppress when tab is active (selected and tool window has focus). */
        @SerialName("whenActive")
        WHEN_ACTIVE,

        /** Suppress when tab is visible (selected and tool window is open). */
        @SerialName("whenVisible")
        WHEN_VISIBLE,
        ;

        fun matches(state: TermbackTabState): Boolean =
            when (this) {
                NONE -> false
                WHEN_ACTIVE -> state == TermbackTabState.VISIBLE_ACTIVE
                WHEN_VISIBLE -> state == TermbackTabState.VISIBLE_INACTIVE || state == TermbackTabState.VISIBLE_ACTIVE
            }
    }

    /**
     * Controls behavior when a new notification arrives for the same tab.
     */
    @Serializable
    enum class OnNext {
        /** Keep this notification when a new one arrives. */
        @SerialName("keep")
        KEEP,

        /** Expire this notification when a new one arrives. */
        @SerialName("expire")
        EXPIRE,
    }

    private fun validate(): ParseResult {
        if (sessionId.value.isBlank()) {
            return ParseResult.Error("sessionId must be a non-empty string")
        }
        if (message.isBlank()) {
            return ParseResult.Error("message must be a non-empty string")
        }
        return ParseResult.Success(this)
    }

    sealed interface ParseResult {
        data class Success(
            val request: TermbackNotificationRequest,
        ) : ParseResult

        data class Error(
            val reason: String,
        ) : ParseResult
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun parse(jsonString: String): ParseResult =
            try {
                // decodeFromString may throw IllegalArgumentException or its subclass SerializationException.
                // https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-json/kotlinx.serialization.json/-json/decode-from-string.html
                json.decodeFromString<TermbackNotificationRequest>(jsonString).validate()
            } catch (e: IllegalArgumentException) {
                ParseResult.Error(e.message ?: "Invalid JSON")
            }
    }
}
