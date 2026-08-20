dependencies {
    api(project(":databag-common"))
    api(libs.gson)
    api(libs.jetbrains.annotations)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
