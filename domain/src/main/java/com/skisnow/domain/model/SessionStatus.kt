package com.skisnow.domain.model

/**
 * Lifecycle of a ski day session.
 * Transitions: Idle surface → Recording ↔ Paused → Stopping → Saved | Failed.
 */
enum class SessionStatus {
    RECORDING,
    PAUSED,
    STOPPING,
    SAVED,
    FAILED,
}
