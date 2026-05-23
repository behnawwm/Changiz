package ir.behnawwm.changiz.model

data class ChangizEntry(
    val fileName: String,
    val type: BumpType,
    val modules: List<String>,
    val author: String? = null,
    val ticket: String? = null,
    val public: Map<String, String>,
    val internal: Map<String, String>
) {
    val isEmpty: Boolean get() = type == BumpType.EMPTY

    fun internalOrPublic(lang: String): String =
        internal[lang]?.takeIf { it.isNotBlank() } ?: public[lang] ?: ""
}
