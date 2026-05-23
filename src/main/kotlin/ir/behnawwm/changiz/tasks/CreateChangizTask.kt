package ir.behnawwm.changiz.tasks

import ir.behnawwm.changiz.config.ConfigParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class CreateChangizTask : DefaultTask() {

    @get:Input
    @get:Option(option = "empty", description = "Create an empty changiz entry (no changelog, no version bump)")
    var empty: Boolean = false

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

        if (empty) {
            val file = changizDir.resolve(fileName)
            file.writeText("type: empty\n")
            logger.lifecycle("✅ Created empty changiz: ${file.relativeTo(project.rootDir)}")
            return
        }

        val type = prompt("Bump type (major/minor/patch/empty)", "patch")
        if (type == "empty") {
            changizDir.resolve(fileName).writeText("type: empty\n")
            logger.lifecycle("✅ Created empty changiz: .changiz/$fileName")
            return
        }

        val modules = prompt("Affected modules (comma-separated, e.g. :app,:feature:auth)", "")
        val ticket = prompt("Ticket ID (leave empty to skip)", "")

        val internalEntries = mutableMapOf<String, String>()
        val publicEntries = mutableMapOf<String, String>()

        for (lang in config.internalLanguages) {
            internalEntries[lang] = prompt("Internal changelog ($lang) [required]", "")
        }
        for (lang in config.publicLanguages) {
            val pub = prompt("Public changelog ($lang) [optional, for app stores]", "")
            if (pub.isNotBlank()) publicEntries[lang] = pub
        }

        val yaml = buildString {
            appendLine("type: $type")
            if (modules.isNotBlank()) {
                appendLine("modules:")
                modules.split(",").map { it.trim() }.forEach { appendLine("  - $it") }
            }
            if (ticket.isNotBlank()) appendLine("ticket: $ticket")
            appendLine("author: ${runGit("config", "user.name").trim()}")
            appendLine("internal:")
            internalEntries.forEach { (lang, text) -> appendLine("  $lang: \"${text.escape()}\"") }
            if (publicEntries.isNotEmpty()) {
                appendLine("public:")
                publicEntries.forEach { (lang, text) -> appendLine("  $lang: \"${text.escape()}\"") }
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
