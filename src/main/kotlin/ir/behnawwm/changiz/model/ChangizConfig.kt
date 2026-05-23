package ir.behnawwm.changiz.model

data class ChangizConfig(
    val internalLanguages: List<String> = listOf("en"),
    val publicLanguages: List<String> = listOf("en", "fa"),
    val publicMaxLength: Int = 500,
    val publicRequired: Boolean = false,
    val internalRequired: Boolean = true,
    val versionFile: String = "version.properties",
    val pathsRequiringChangiz: List<String> = listOf("*/src/**", "*.gradle.kts"),
    val pathsExcluded: List<String> = listOf(".changiz/**", "*.md", ".gitlab-ci.yml"),
    val branchPattern: String? = null
)
