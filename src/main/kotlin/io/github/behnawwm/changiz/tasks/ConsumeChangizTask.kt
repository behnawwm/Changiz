package io.github.behnawwm.changiz.tasks

import io.github.behnawwm.changiz.config.ChangizParser
import io.github.behnawwm.changiz.config.ConfigParser
import io.github.behnawwm.changiz.model.BumpType
import io.github.behnawwm.changiz.model.ChangizEntry
import io.github.behnawwm.changiz.model.Version
import io.github.behnawwm.changiz.render.ChangelogRenderer
import io.github.behnawwm.changiz.render.MarketRenderer
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.time.LocalDate
import java.util.Properties

abstract class ConsumeChangizTask : DefaultTask() {

    @TaskAction
    fun consume() {
        val config = ConfigParser.parse(project.rootDir)
        val entries = ChangizParser.parseAll(project.rootDir)

        if (entries.isEmpty()) {
            throw GradleException("No changiz entries to consume. Nothing to release.")
        }

        val effectiveEntries = entries.filter { it.type != BumpType.EMPTY }
        if (effectiveEntries.isEmpty()) {
            logger.lifecycle("All entries are 'empty'. No version bump or changelog generated.")
            cleanupEntries(entries)
            return
        }

        val bumpType = effectiveEntries.maxOf { it.type }
        val versionFile = project.rootDir.resolve(config.versionFile)
        val currentVersion = readVersion(versionFile)
        val newVersion = currentVersion.bump(bumpType)
        val today = LocalDate.now().toString()

        logger.lifecycle("Bumping version: $currentVersion → $newVersion ($bumpType)")

        val versionDir = project.rootDir.resolve("changelogs/versions/$newVersion")
        versionDir.mkdirs()

        // Generate public market files (multi-language)
        config.publicLanguages.forEach { lang ->
            versionDir.resolve("public_$lang.txt").writeText(MarketRenderer.render(effectiveEntries, lang))
        }

        // Generate internal changelog (English only by default)
        config.internalLanguages.forEach { lang ->
            versionDir.resolve("internal_$lang.md").writeText(ChangelogRenderer.renderVersionInternal(effectiveEntries, lang))
        }

        versionDir.resolve("meta.yaml").writeText(buildString {
            appendLine("version: $newVersion")
            appendLine("version_code: ${newVersion.code}")
            appendLine("date: $today")
            appendLine("bump_type: ${bumpType.name.lowercase()}")
            appendLine("entries_consumed:")
            entries.forEach { appendLine("  - ${it.fileName}") }
        })

        val changelogFile = project.rootDir.resolve("changelogs/CHANGELOG.md")
        prependToFile(changelogFile, ChangelogRenderer.renderFullBlock(effectiveEntries, newVersion, today))

        val publicChangelogFile = project.rootDir.resolve("changelogs/CHANGELOG_PUBLIC.md")
        prependToFile(publicChangelogFile, ChangelogRenderer.renderPublicBlock(effectiveEntries, newVersion, today, config.publicLanguages))

        writeVersion(versionFile, newVersion)
        cleanupEntries(entries)

        logger.lifecycle("✅ Released $newVersion")
    }

    private fun readVersion(file: File): Version {
        if (!file.exists()) return Version(0, 1, 0)
        val props = Properties().apply { load(file.inputStream()) }
        return Version(
            props.getProperty("VERSION_MAJOR", "0").toInt(),
            props.getProperty("VERSION_MINOR", "1").toInt(),
            props.getProperty("VERSION_PATCH", "0").toInt()
        )
    }

    private fun writeVersion(file: File, version: Version) {
        file.parentFile?.mkdirs()
        file.writeText("VERSION_MAJOR=${version.major}\nVERSION_MINOR=${version.minor}\nVERSION_PATCH=${version.patch}\nVERSION_CODE=${version.code}\n")
    }

    private fun prependToFile(file: File, content: String) {
        file.parentFile?.mkdirs()
        val existing = if (file.exists()) file.readText() else "# Changelog\n\n"
        val header = existing.lines().firstOrNull { it.startsWith("#") } ?: "# Changelog"
        val body = existing.removePrefix(header).trimStart('\n')
        file.writeText("$header\n\n$content\n$body")
    }

    private fun cleanupEntries(entries: List<ChangizEntry>) {
        val dir = project.rootDir.resolve(".changiz")
        entries.forEach { dir.resolve(it.fileName).delete() }
    }
}
