plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    signing
}

group = "ir.behnawwm.changiz"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
}

gradlePlugin {
    plugins {
        create("changiz") {
            id = "ir.behnawwm.changiz"
            displayName = "Changiz"
            description = "Changelog enforcer for Android/Gradle/Kotlin projects"
            implementationClass = "ir.behnawwm.changiz.ChangizPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Changiz")
                description.set("Changelog enforcer for Android/Gradle/Kotlin projects")
                url.set("https://github.com/behnawwm/changiz")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("behnawwm")
                        name.set("Behnam")
                        url.set("https://github.com/behnawwm")
                    }
                }
                scm {
                    url.set("https://github.com/behnawwm/changiz")
                    connection.set("scm:git:git://github.com/behnawwm/changiz.git")
                    developerConnection.set("scm:git:ssh://github.com/behnawwm/changiz.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            credentials {
                username = findProperty("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME")
                password = findProperty("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

tasks.withType<Sign>().configureEach {
    onlyIf { !version.toString().endsWith("SNAPSHOT") }
}
