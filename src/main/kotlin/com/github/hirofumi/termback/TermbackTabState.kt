package com.github.hirofumi.termback

/**
 * Represents the visibility state of a terminal tab.
 */
enum class TermbackTabState {
    /** Tab is not selected, or the Terminal tool window is not visible. */
    NOT_VISIBLE,

    /** Tab is selected but the Terminal tool window does not have focus. */
    VISIBLE_INACTIVE,

    /** Tab is selected and the Terminal tool window has focus. */
    VISIBLE_ACTIVE,
}
