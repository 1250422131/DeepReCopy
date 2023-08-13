import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "com.imcys.deeprecopy"
version = "0.0.1Alpha-05"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val signingKeyId: String = gradleLocalProperties(rootDir).getProperty("signing.keyId")
val signingPassword: String = gradleLocalProperties(rootDir).getProperty("signing.password")
val secretKeyRingFile: String =
    gradleLocalProperties(rootDir).getProperty("signing.secretKeyRingFile")
val ossrhUsername: String = gradleLocalProperties(rootDir).getProperty("ossrhUsername")
val ossrhPassword: String = gradleLocalProperties(rootDir).getProperty("ossrhPassword")
val PUBLISH_NAME: String = gradleLocalProperties(rootDir).getProperty("PUBLISH_NAME")
val PUBLISH_GROUP_ID: String = gradleLocalProperties(rootDir).getProperty("PUBLISH_GROUP_ID")
val PUBLISH_EMAIL: String = gradleLocalProperties(rootDir).getProperty("PUBLISH_EMAIL")

group = "com.imcys.deeprecopy"
version = "0.0.1Alpha-05"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "core"
            groupId = "com.imcys.deeprecopy"
            version = "0.0.1Alpha-05"

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
            }
        }
    }

    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
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
