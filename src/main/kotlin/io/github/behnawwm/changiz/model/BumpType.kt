package io.github.behnawwm.changiz.model

enum class BumpType {
    EMPTY, PATCH, MINOR, MAJOR;

    companion object {
        fun fromString(value: String): BumpType = when (value.lowercase()) {
            "major" -> MAJOR
            "minor" -> MINOR
            "patch" -> PATCH
            "empty" -> EMPTY
            else -> throw IllegalArgumentException("Invalid bump type: $value. Must be one of: major, minor, patch, empty")
        }
    }
}
