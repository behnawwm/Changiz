package ir.behnawwm.changiz.model

enum class BumpType {
    NONE, PATCH, MINOR, MAJOR;

    companion object {
        fun fromString(value: String): BumpType = when (value.lowercase()) {
            "major" -> MAJOR
            "minor" -> MINOR
            "patch" -> PATCH
            "none" -> NONE
            else -> throw IllegalArgumentException("Invalid bump type: $value. Must be one of: major, minor, patch, none")
        }
    }
}
