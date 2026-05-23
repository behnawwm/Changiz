package ir.behnawwm.changiz.tasks

import ir.behnawwm.changiz.config.ConfigParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CreateChangizTask : DefaultTask() {

    @TaskAction
    fun create() {
        val config = ConfigParser.parse(project.rootDir)
        val changizDir = project.rootDir.resolve(".changiz")
        changizDir.mkdirs()

        val branchName = runGit("rev-parse", "--abbrev-ref", "HEAD").trim()
        val slug = branchName.replace(Regex("[^a-zA-Z0-9]"), "-").lowercase().take(60)

        var fileName = "$slug.yaml"
        var counter = 2
        while (changizDir.resolve(fileName).exists()) {
            fileName = "$slug-$counter.yaml"
            counter++
        }

        val type = prompt("Bump type (major/minor/patch/none)", "patch")
        val modules = prompt("Affected modules (comma-separated, e.g. :app,:feature:auth)", "")
        val ticket = prompt("Ticket ID (leave empty to skip)", "")

        val publicEntries = mutableMapOf<String, String>()
        val internalEntries = mutableMapOf<String, String>()

        for (lang in config.languages) {
            publicEntries[lang] = prompt("Public changelog ($lang)", "")
            val internal = prompt("Internal changelog ($lang) [leave empty to use public]", "")
            if (internal.isNotBlank()) {
                internalEntries[lang] = internal
            }
        }

        val yaml = buildString {
            appendLine("type: $type")
            if (modules.isNotBlank()) {
                appendLine("modules:")
                modules.split(",").map { it.trim() }.forEach { appendLine("  - $it") }
            }
            if (ticket.isNotBlank()) appendLine("ticket: $ticket")
            appendLine("author: ${runGit("config", "user.name").trim()}")
            appendLine("public:")
            publicEntries.forEach { (lang, text) -> appendLine("  $lang: \"${text.escape()}\"") }
            if (internalEntries.isNotEmpty()) {
                appendLine("internal:")
                internalEntries.forEach { (lang, text) -> appendLine("  $lang: \"${text.escape()}\"") }
            }
        }

        val file = changizDir.resolve(fileName)
        file.writeText(yaml)
        logger.lifecycle("✅ Created ${file.relativeTo(project.rootDir)}")
    }

    private fun prompt(message: String, default: String): String {
        print("$message${if (default.isNotBlank()) " [$default]" else ""}: ")
        val input = readlnOrNull()?.trim() ?: ""
        return input.ifBlank { default }
    }

    private fun runGit(vararg args: String): String =
        ProcessBuilder("git", *args)
            .directory(project.rootDir)
            .redirectErrorStream(true)
            .start()
            .inputStream.bufferedReader().readText()

    private fun String.escape(): String = replace("\"", "\\\"")
}
