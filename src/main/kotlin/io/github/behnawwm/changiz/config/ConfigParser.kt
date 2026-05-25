package io.github.behnawwm.changiz.config

import io.github.behnawwm.changiz.model.ChangizConfig
import org.yaml.snakeyaml.Yaml
import java.io.File

object ConfigParser {

    fun parse(projectDir: File): ChangizConfig {
        val configFile = projectDir.resolve(".changiz/config.yaml")
        if (!configFile.exists()) return ChangizConfig()

        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(configFile.readText()) ?: return ChangizConfig()

        @Suppress("UNCHECKED_CAST")
        val changelog = data["changelog_types"] as? Map<String, Map<String, Any>>
        val languages = (data["languages"] as? Map<String, Any>)

        return ChangizConfig(
            internalLanguages = (languages?.get("internal") as? List<*>)?.map { it.toString() } ?: listOf("en"),
            publicLanguages = (languages?.get("public") as? List<*>)?.map { it.toString() } ?: listOf("en", "fa"),
            publicMaxLength = changelog?.get("public")?.get("max_length")?.toString()?.toIntOrNull() ?: 500,
            publicRequired = changelog?.get("public")?.get("required")?.toString()?.toBooleanStrictOrNull() ?: false,
            internalRequired = changelog?.get("internal")?.get("required")?.toString()?.toBooleanStrictOrNull() ?: true,
            versionFile = data["version_file"]?.toString() ?: "version.properties",
            pathsRequiringChangiz = (data["paths_requiring_changiz"] as? List<*>)?.map { it.toString() } ?: listOf("*/src/**"),
            pathsExcluded = (data["paths_excluded"] as? List<*>)?.map { it.toString() } ?: listOf(".changiz/**", "*.md"),
            branchPattern = data["branch_pattern"]?.toString()
        )
    }
}
