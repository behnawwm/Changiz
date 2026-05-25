package io.github.behnawwm.changiz.config

import io.github.behnawwm.changiz.model.BumpType
import io.github.behnawwm.changiz.model.ChangizEntry
import org.yaml.snakeyaml.Yaml
import java.io.File

object ChangizParser {

    fun parseAll(projectDir: File): List<ChangizEntry> {
        val dir = projectDir.resolve(".changiz")
        if (!dir.exists()) return emptyList()
        return dir.listFiles()
            ?.filter { it.extension == "yaml" && it.name != "config.yaml" }
            ?.map { parse(it) }
            ?: emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    fun parse(file: File): ChangizEntry {
        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(file.readText())
        return ChangizEntry(
            fileName = file.name,
            type = BumpType.fromString(data["type"]?.toString() ?: "patch"),
            modules = (data["modules"] as? List<*>)?.map { it.toString() } ?: emptyList(),
            author = data["author"]?.toString(),
            ticket = data["ticket"]?.toString(),
            public = (data["public"] as? Map<String, Any>)?.mapValues { it.value.toString() } ?: emptyMap(),
            internal = (data["internal"] as? Map<String, Any>)?.mapValues { it.value.toString() } ?: emptyMap()
        )
    }
}
