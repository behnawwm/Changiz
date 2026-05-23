package ir.behnawwm.changiz

import ir.behnawwm.changiz.tasks.CheckChangizExistsTask
import ir.behnawwm.changiz.tasks.ConsumeChangizTask
import ir.behnawwm.changiz.tasks.CreateChangizTask
import ir.behnawwm.changiz.tasks.ValidateChangizTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ChangizPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("createChangiz", CreateChangizTask::class.java) {
            group = "changiz"
            description = "Create a new changiz entry (use --empty for no-op)"
        }
        project.tasks.register("validateChangiz", ValidateChangizTask::class.java) {
            group = "changiz"
            description = "Validate all pending changiz entries"
        }
        project.tasks.register("checkChangizExists", CheckChangizExistsTask::class.java) {
            group = "changiz"
            description = "Check that a changiz entry exists for source changes"
        }
        project.tasks.register("consumeChangiz", ConsumeChangizTask::class.java) {
            group = "changiz"
            description = "Consume changiz entries, bump version, and generate changelogs"
        }
    }
}
