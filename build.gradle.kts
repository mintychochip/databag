import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

val releaseVersionPattern =
    Regex("""\d{2}\.([1-9]|1[0-2])\.([1-9]|[12]\d|3[01])\.[1-9]\d*""")

val requestedReleaseVersion = providers.gradleProperty("releaseVersion")
    .orNull
    ?.takeIf { it.isNotBlank() }
val databagVersion = requestedReleaseVersion ?: "0.0.0-SNAPSHOT"

gradle.taskGraph.whenReady {
    val githubPackagesPublicationRequested = allTasks.any { task ->
        task.name.contains("GitHubPackages")
    }
    val validReleaseVersion =
        requestedReleaseVersion?.matches(releaseVersionPattern) == true
    if (githubPackagesPublicationRequested && !validReleaseVersion) {
        throw GradleException(
            "GitHub Packages publication requires -PreleaseVersion=YY.M.D.REVISION "
                + "(for example, -PreleaseVersion=26.8.19.1).",
        )
    }
}

allprojects {
    group = "dev.mintychochip.databag"
    version = databagVersion
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    val javaVersion = if (name == "databag-paper") 25 else 21
    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
        withSourcesJar()
        withJavadocJar()
    }
    tasks.withType<JavaCompile>().configureEach {
        options.release.set(javaVersion)
    }
    tasks.withType<Javadoc>().configureEach {
        isFailOnError = false
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifactId = project.name
                pom {
                    name.set(project.name)
                    description.set(
                        when (project.name) {
                            "databag-common" ->
                                "PDC-shaped Kryo primitive bag for namespaced byte[] payloads."
                            "databag-api" ->
                                "Condition graph and vanilla loot-condition JSON for databag."
                            "databag-paper" ->
                                "Paper adapter that builds ConditionContext and writes PersistentDataContainer bags."
                            else -> "databag library."
                        },
                    )
                    url.set("https://github.com/mintychochip/databag")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("mintychochip")
                            name.set("mintychochip")
                            url.set("https://github.com/mintychochip")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/mintychochip/databag.git")
                        developerConnection.set(
                            "scm:git:ssh://git@github.com/mintychochip/databag.git",
                        )
                        url.set("https://github.com/mintychochip/databag")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "localBuildRepo"
                url = rootProject.layout.buildDirectory.dir("maven-repo").get().asFile.toURI()
            }
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/mintychochip/databag")
                credentials {
                    username = System.getenv("GITHUB_ACTOR") ?: ""
                    password = System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }
    }

    tasks.withType<PublishToMavenRepository>().configureEach {
        if (name.contains("GitHubPackages")) {
            doFirst {
                val version = requestedReleaseVersion
                require(version != null && version.matches(releaseVersionPattern)) {
                    "Repository publication '$name' requires -PreleaseVersion=YY.M.D.REVISION"
                }
            }
        }
    }
}
