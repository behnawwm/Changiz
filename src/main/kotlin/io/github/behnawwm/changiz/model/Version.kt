package io.github.behnawwm.changiz.model

data class Version(val major: Int, val minor: Int, val patch: Int) {

    fun bump(type: BumpType): Version = when (type) {
        BumpType.MAJOR -> Version(major + 1, 0, 0)
        BumpType.MINOR -> Version(major, minor + 1, 0)
        BumpType.PATCH -> Version(major, minor, patch + 1)
        BumpType.EMPTY -> this
    }

    val code: Int get() = major * 10000 + minor * 100 + patch

    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        fun parse(text: String): Version {
            val parts = text.trim().split(".")
            require(parts.size == 3) { "Version must be in format major.minor.patch, got: $text" }
            return Version(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
    }
}
