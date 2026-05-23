package ir.behnawwm.changiz.tasks

import ir.behnawwm.changiz.config.ConfigParser
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.nio.file.FileSystems

abstract class CheckChangizExistsTask : DefaultTask() {

    @get:Input
    @get:Optional
    @get:Option(option = "targetBranch", description = "Target branch to diff against")
    var targetBranch: String? = null

    @TaskAction
    fun check() {
        val config = ConfigParser.parse(project.rootDir)
        val target = targetBranch ?: project.findProperty("targetBranch")?.toString() ?: "origin/develop"

        val changedFiles = runGit("diff", "--name-only", "$target...HEAD")
            .lines()
            .filter { it.isNotBlank() }

        if (changedFiles.isEmpty()) {
            logger.lifecycle("No changed files detected.")
            return
        }

        val requiresChangiz = changedFiles.any { file ->
            matchesAny(file, config.pathsRequiringChangiz) && !matchesAny(file, config.pathsExcluded)
        }

        if (!requiresChangiz) {
            logger.lifecycle("✅ No changiz entry required (changes don't match enforced paths).")
            return
        }

        val hasChangiz = changedFiles.any {
            it.startsWith(".changiz/") && it.endsWith(".yaml") && it != ".changiz/config.yaml"
        }

        if (!hasChangiz) {
            throw GradleException(
                """
                |❌ No changiz entry found for this branch.
                |
                |Source files were modified but no changiz entry was added.
                |Run: ./gradlew createChangiz
                |
                |If this change doesn't need a changelog entry, create an entry with type: none
                """.trimMargin()
            )
        }

        logger.lifecycle("✅ Changiz entry found.")
    }

    private fun matchesAny(path: String, patterns: List<String>): Boolean {
        val fs = FileSystems.getDefault()
        return patterns.any { pattern ->
            fs.getPathMatcher("glob:$pattern").matches(fs.getPath(path))
        }
    }

    private fun runGit(vararg args: String): String =
        ProcessBuilder("git", *args)
            .directory(project.rootDir)
            .redirectErrorStream(true)
            .start()
            .inputStream.bufferedReader().readText()
}
