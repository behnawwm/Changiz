package ir.behnawwm.changiz.render

import ir.behnawwm.changiz.model.BumpType
import ir.behnawwm.changiz.model.ChangizEntry

object MarketRenderer {

    fun render(entries: List<ChangizEntry>, lang: String): String = buildString {
        entries
            .filter { it.type != BumpType.NONE }
            .mapNotNull { it.public[lang] }
            .forEach { appendLine("• $it") }
    }.trimEnd()
}
