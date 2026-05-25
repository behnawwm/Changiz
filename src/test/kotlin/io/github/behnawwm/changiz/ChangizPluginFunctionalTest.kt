package io.github.behnawwm.changiz

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ChangizPluginFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private val buildFile get() = projectDir.resolve("build.gradle.kts")
    private val settingsFile get() = projectDir.resolve("settings.gradle.kts")
    private val changizDir get() = projectDir.resolve(".changiz")
    private val versionFile get() = projectDir.resolve("version.properties")

    @BeforeEach
    fun setup() {
        settingsFile.writeText("rootProject.name = \"test-project\"")
        buildFile.writeText("""
            plugins {
                id("io.github.behnawwm.changiz")
            }
        """.trimIndent())
        changizDir.mkdirs()
        changizDir.resolve("config.yaml").writeText("""
            languages:
              internal:
                - en
              public:
                - en
                - fa
            changelog_types:
              public:
                max_length: 500
                required: false
              internal:
                required: true
            version_file: version.properties
            paths_requiring_changiz:
              - "*/src/**"
              - "*.gradle.kts"
            paths_excluded:
              - ".changiz/**"
              - "*.md"
        """.trimIndent())
        versionFile.writeText("VERSION_MAJOR=1\nVERSION_MINOR=0\nVERSION_PATCH=0\nVERSION_CODE=10000\n")
    }

    private fun runner(vararg args: String) = GradleRunner.create()
        .withProjectDir(projectDir)
        .withArguments(*args)
        .withPluginClasspath()

    @Test
    fun `plugin registers all tasks`() {
        val result = runner("tasks", "--group=changiz").build()
        val output = result.output
        assertTrue(output.contains("createChangiz"))
        assertTrue(output.contains("validateChangiz"))
        assertTrue(output.contains("checkChangizExists"))
        assertTrue(output.contains("consumeChangiz"))
    }

    @Test
    fun `validateChangiz passes with no entries`() {
        val result = runner("validateChangiz").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":validateChangiz")?.outcome)
        assertTrue(result.output.contains("No changiz entries to validate"))
    }

    @Test
    fun `validateChangiz passes with valid entry`() {
        changizDir.resolve("test-entry.yaml").writeText("""
            type: patch
            internal:
              en: "Fixed a bug"
        """.trimIndent())

        val result = runner("validateChangiz").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":validateChangiz")?.outcome)
        assertTrue(result.output.contains("valid"))
    }

    @Test
    fun `validateChangiz fails when internal changelog missing`() {
        changizDir.resolve("test-entry.yaml").writeText("""
            type: patch
            public:
              en: "Fixed a bug"
        """.trimIndent())

        val result = runner("validateChangiz").buildAndFail()
        assertEquals(TaskOutcome.FAILED, result.task(":validateChangiz")?.outcome)
        assertTrue(result.output.contains("Missing required internal changelog"))
    }

    @Test
    fun `validateChangiz skips validation for empty entries`() {
        changizDir.resolve("test-entry.yaml").writeText("type: empty\n")

        val result = runner("validateChangiz").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":validateChangiz")?.outcome)
    }

    @Test
    fun `validateChangiz fails when public changelog exceeds max length`() {
        val longText = "x".repeat(501)
        changizDir.resolve("test-entry.yaml").writeText("""
            type: patch
            internal:
              en: "Fixed a bug"
            public:
              en: "$longText"
        """.trimIndent())

        val result = runner("validateChangiz").buildAndFail()
        assertTrue(result.output.contains("exceeds 500 chars"))
    }

    @Test
    fun `consumeChangiz bumps version and generates changelogs`() {
        changizDir.resolve("feat-login.yaml").writeText("""
            type: minor
            modules:
              - :app
            ticket: JIRA-100
            internal:
              en: "Added login feature"
            public:
              en: "New login screen"
              fa: "صفحه ورود جدید"
        """.trimIndent())

        val result = runner("consumeChangiz").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":consumeChangiz")?.outcome)

        // Version bumped
        val props = versionFile.readText()
        assertTrue(props.contains("VERSION_MAJOR=1"))
        assertTrue(props.contains("VERSION_MINOR=1"))
        assertTrue(props.contains("VERSION_PATCH=0"))
        assertTrue(props.contains("VERSION_CODE=10100"))

        // Changelog generated
        val changelog = projectDir.resolve("changelogs/CHANGELOG.md")
        assertTrue(changelog.exists())
        assertTrue(changelog.readText().contains("1.1.0"))
        assertTrue(changelog.readText().contains("Added login feature"))

        // Market files generated
        val publicEn = projectDir.resolve("changelogs/versions/1.1.0/public_en.txt")
        assertTrue(publicEn.exists())
        assertTrue(publicEn.readText().contains("New login screen"))

        val publicFa = projectDir.resolve("changelogs/versions/1.1.0/public_fa.txt")
        assertTrue(publicFa.exists())
        assertTrue(publicFa.readText().contains("صفحه ورود جدید"))

        // Entry deleted
        assertFalse(changizDir.resolve("feat-login.yaml").exists())
    }

    @Test
    fun `consumeChangiz with multiple entries picks highest bump`() {
        changizDir.resolve("fix.yaml").writeText("""
            type: patch
            internal:
              en: "Bug fix"
        """.trimIndent())
        changizDir.resolve("feat.yaml").writeText("""
            type: minor
            internal:
              en: "New feature"
        """.trimIndent())

        runner("consumeChangiz").build()

        val props = versionFile.readText()
        assertTrue(props.contains("VERSION_MINOR=1")) // minor wins over patch
    }

    @Test
    fun `consumeChangiz with only empty entries does not bump`() {
        changizDir.resolve("empty.yaml").writeText("type: empty\n")

        val result = runner("consumeChangiz").build()
        assertTrue(result.output.contains("No version bump"))

        val props = versionFile.readText()
        assertTrue(props.contains("VERSION_MINOR=0")) // unchanged
        assertFalse(changizDir.resolve("empty.yaml").exists()) // still cleaned up
    }

    @Test
    fun `consumeChangiz fails with no entries`() {
        val result = runner("consumeChangiz").buildAndFail()
        assertTrue(result.output.contains("No changiz entries to consume"))
    }

    @Test
    fun `consumeChangiz generates meta yaml`() {
        changizDir.resolve("test.yaml").writeText("""
            type: patch
            internal:
              en: "Fix"
        """.trimIndent())

        runner("consumeChangiz").build()

        val meta = projectDir.resolve("changelogs/versions/1.0.1/meta.yaml")
        assertTrue(meta.exists())
        assertTrue(meta.readText().contains("version: 1.0.1"))
        assertTrue(meta.readText().contains("test.yaml"))
    }
}
