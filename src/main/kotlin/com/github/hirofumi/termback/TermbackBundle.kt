package com.github.hirofumi.termback

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

@NonNls
private const val BUNDLE = "messages.TermbackBundle"

// Delegation pattern as recommended:
// https://plugins.jetbrains.com/docs/intellij/internationalization.html#message-bundle-class
internal object TermbackBundle {
    private val INSTANCE = DynamicBundle(TermbackBundle::class.java, BUNDLE)

    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any,
    ): @Nls String = INSTANCE.getMessage(key, *params)

    fun lazyMessage(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any,
    ): Supplier<@Nls String> = INSTANCE.getLazyMessage(key, *params)
}
