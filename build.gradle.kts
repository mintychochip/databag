plugins {
    `java-library`
    `maven-publish`
}

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

group = "dev.databag"
version = databagVersion

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.withType<Javadoc>().configureEach {
    isFailOnError = false
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    api(libs.adventure.api)
    implementation(libs.kryo)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "databag"
            pom {
                name.set("databag")
                description.set(
                    "PDC-shaped Kryo primitive bag for namespaced byte[] payloads.",
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
                    developerConnection.set("scm:git:ssh://git@github.com/mintychochip/databag.git")
                    url.set("https://github.com/mintychochip/databag")
                }
            }
        }
    }
    repositories {
        maven {
            name = "localBuildRepo"
            url = layout.buildDirectory.dir("maven-repo").get().asFile.toURI()
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

tasks.withType<org.gradle.api.publish.maven.tasks.PublishToMavenRepository>().configureEach {
    if (name.contains("GitHubPackages")) {
        doFirst {
            val version = requestedReleaseVersion
            require(version != null && version.matches(releaseVersionPattern)) {
                "Repository publication '$name' requires -PreleaseVersion=YY.M.D.REVISION"
            }
        }
    }
}
