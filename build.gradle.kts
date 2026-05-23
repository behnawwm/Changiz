plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = property("GROUP").toString()
version = property("VERSION").toString()

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    website.set("https://github.com/behnawwm/changiz")
    vcsUrl.set("https://github.com/behnawwm/changiz")

    plugins {
        create("changiz") {
            id = "ir.behnawwm.changiz"
            displayName = "Changiz"
            description = "Changelog enforcer for Android/Gradle/Kotlin projects"
            tags.set(listOf("changelog", "android", "kotlin", "versioning", "ci"))
            implementationClass = "ir.behnawwm.changiz.ChangizPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/behnawwm/changiz")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
