@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("maven-publish")
}

group = "com.imcys.deeprecopy"
version = "0.0.1Alpha-05"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":core"))
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlin.reflect)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "compiler"
            from(components["java"])
        }
    }
}
