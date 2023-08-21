import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("java-library")
    id("maven-publish")
    id("signing")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlin.reflect)
}

val signingKeyId: String = gradleLocalProperties(rootDir).getProperty("signing.keyId") ?: ""
val signingPassword: String = gradleLocalProperties(rootDir).getProperty("signing.password") ?: ""
val secretKeyRingFile: String =
    gradleLocalProperties(rootDir).getProperty("signing.secretKeyRingFile") ?: ""
val ossrhUsername: String = gradleLocalProperties(rootDir).getProperty("ossrhUsername") ?: ""
val ossrhPassword: String = gradleLocalProperties(rootDir).getProperty("ossrhPassword") ?: ""
val PUBLISH_NAME: String = gradleLocalProperties(rootDir).getProperty("PUBLISH_NAME") ?: ""
val PUBLISH_GROUP_ID: String = gradleLocalProperties(rootDir).getProperty("PUBLISH_GROUP_ID") ?: ""
val PUBLISH_EMAIL: String = gradleLocalProperties(rootDir).getProperty("PUBLISH_EMAIL") ?: ""

val mGroupId = "com.imcys.deeprecopy"
val mVersion = "0.0.1-Alpha-13"
val mArtifactId = "compiler"

group = mGroupId
version = mVersion

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = mArtifactId
            groupId = mGroupId
            version = mVersion

            // 配置额外的 artifact，如 javadocJar 和 sourcesJar
            from(components["java"])

            pom {
                name.value("DeepReCopy")
                description.value("DeepReCopy is a deep copy utility library developed specifically for Kotlin's Data classes. It utilizes KSP (Kotlin Symbol Processing) to generate deep copy extension methods for Data classes, providing support for DSL (Domain-Specific Language) syntax.")

                url.value("https://github.com/1250422131/DeepReCopy")

                developers {
                    developer {
                        id.value(PUBLISH_NAME)
                        name.value(PUBLISH_NAME)
                        email.value(PUBLISH_EMAIL)
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/1250422131/DeepReCopy.git")
                    developerConnection.set("scm:git:https://github.com/1250422131/DeepReCopy.git")
                    url.set("https://github.com/1250422131/DeepReCopy")
                }

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        distribution.set("repo")
                    }
                }
            }
        }
    }

    repositories {

        maven {
            val releasesRepoUrl =
                "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

            url = uri(
                if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl,
            )
            credentials {
                // 你的sonatype账号
                username = ossrhUsername
                // 你的sonatype密码
                password = ossrhPassword
            }
        }
    }
}

// signing的配置数据在编译后必须是project的properties，即在全局可识别。
// 这里的配置的数据可供整个Project全局访问。
gradle.taskGraph.whenReady {
    allprojects {
        ext["signing.keyId"] = signingKeyId
        ext["signing.password"] = signingPassword
        ext["signing.secretKeyRingFile"] = secretKeyRingFile
    }
}
// 这里是使用gpg秘钥进行签名
signing {
    sign(publishing.publications)
}
