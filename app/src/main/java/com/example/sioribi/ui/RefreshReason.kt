package com.example.sioribi.ui

enum class RefreshReason {
    Added,
    Updated,
    OptionsChanged,
    FirstRender,
    Manual,
    TimeChanged,
    Boot,
    PackageReplaced,
    Periodic,
    Unknown,
}

internal fun refreshReasonFromName(name: String?): RefreshReason =
    name?.let { raw ->
        RefreshReason.values().firstOrNull { it.name == raw }
    } ?: RefreshReason.Unknown
