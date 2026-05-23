package ir.behnawwm.changiz.tasks

import ir.behnawwm.changiz.config.ChangizParser
import ir.behnawwm.changiz.config.ConfigParser
import ir.behnawwm.changiz.model.BumpType
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

abstract class ValidateChangizTask : DefaultTask() {

    @TaskAction
    fun validate() {
        val config = ConfigParser.parse(project.rootDir)
        val entries = ChangizParser.parseAll(project.rootDir)

        if (entries.isEmpty()) {
            logger.lifecycle("No changiz entries to validate.")
            return
        }

        val errors = mutableListOf<String>()

        entries.forEach { entry ->
            // Empty entries skip all validation
            if (entry.type == BumpType.EMPTY) return@forEach

            // Internal is required
            if (config.internalRequired) {
                config.languages.forEach { lang ->
                    if (entry.internal[lang].isNullOrBlank()) {
                        errors += "[${entry.fileName}] Missing required internal changelog for language: $lang"
                    }
                }
            }

            // Public is optional but if present, check max length
            if (config.publicRequired) {
                config.languages.forEach { lang ->
                    if (entry.public[lang].isNullOrBlank()) {
                        errors += "[${entry.fileName}] Missing required public changelog for language: $lang"
                    }
                }
            }

            entry.public.forEach { (lang, text) ->
                if (text.length > config.publicMaxLength) {
                    errors += "[${entry.fileName}] Public changelog ($lang) exceeds ${config.publicMaxLength} chars (${text.length})"
                }
            }
        }

        if (errors.isNotEmpty()) {
            errors.forEach { logger.error("❌ $it") }
            throw GradleException("Changiz validation failed with ${errors.size} error(s)")
        }

        logger.lifecycle("✅ All ${entries.size} changiz entry(ies) are valid.")
    }
}
