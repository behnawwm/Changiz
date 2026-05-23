package ir.behnawwm.changiz.model

data class ChangizConfig(
    val languages: List<String> = listOf("en", "fa"),
    val publicMaxLength: Int = 500,
    val publicRequired: Boolean = true,
    val internalRequired: Boolean = false,
    val versionFile: String = "version.properties",
    val pathsRequiringChangiz: List<String> = listOf("*/src/**", "*.gradle.kts"),
    val pathsExcluded: List<String> = listOf(".changiz/**", "*.md", ".gitlab-ci.yml"),
    val branchPattern: String? = null
)
