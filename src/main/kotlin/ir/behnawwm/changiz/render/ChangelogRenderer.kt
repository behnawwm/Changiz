package ir.behnawwm.changiz.render

import ir.behnawwm.changiz.model.BumpType
import ir.behnawwm.changiz.model.ChangizEntry
import ir.behnawwm.changiz.model.Version

object ChangelogRenderer {

    fun renderFullBlock(entries: List<ChangizEntry>, version: Version, date: String): String = buildString {
        appendLine("## $version ($date)")
        appendLine()
        for (type in listOf(BumpType.MAJOR, BumpType.MINOR, BumpType.PATCH)) {
            val items = entries.filter { it.type == type }
            if (items.isEmpty()) continue
            appendLine("### ${type.name.lowercase().replaceFirstChar { it.uppercase() }}")
            items.forEach { entry ->
                val prefix = entry.ticket?.let { "[$it] " } ?: ""
                val modules = if (entry.modules.isNotEmpty()) " (${entry.modules.joinToString(", ")})" else ""
                appendLine("- $prefix${entry.internalOrPublic("en")}$modules")
            }
            appendLine()
        }
        appendLine("---")
    }

    fun renderPublicBlock(entries: List<ChangizEntry>, version: Version, date: String, languages: List<String>): String = buildString {
        appendLine("## $version ($date)")
        appendLine()
        languages.forEach { lang ->
            if (languages.size > 1) appendLine("### $lang")
            entries.filter { it.type != BumpType.EMPTY }.forEach { entry ->
                val text = entry.public[lang] ?: return@forEach
                if (text.isNotBlank()) appendLine("- $text")
            }
            appendLine()
        }
        appendLine("---")
    }

    fun renderVersionInternal(entries: List<ChangizEntry>, lang: String): String = buildString {
        for (type in listOf(BumpType.MAJOR, BumpType.MINOR, BumpType.PATCH)) {
            val items = entries.filter { it.type == type }
            if (items.isEmpty()) continue
            appendLine("### ${type.name.lowercase().replaceFirstChar { it.uppercase() }}")
            items.forEach { entry ->
                val prefix = entry.ticket?.let { "[$it] " } ?: ""
                appendLine("- $prefix${entry.internalOrPublic(lang)}")
            }
            appendLine()
        }
    }
}
