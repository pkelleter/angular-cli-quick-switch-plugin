import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "patrick.kelleter"
version = "2.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdeaCommunity("2024.2.6")
        testFramework(TestFrameworkType.Platform)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

intellijPlatform {
    pluginConfiguration {
        changeNotes = """
            <ul>
                <li>Updated for IntelliJ Platform 2024.2 and newer.</li>
                <li>Added a settings page with optional previous-tab closing.</li>
                <li>Fixed switching in editor splits and removed tab-closing side effects by default.</li>
                <li>Improved support for virtual files and modern IntelliJ action APIs.</li>
            </ul>
        """.trimIndent()

        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            current()
            recommended()
            create(IntelliJPlatformType.IntellijIdea, "2025.3.6.1")
            create(IntelliJPlatformType.IntellijIdea, "2026.1.5")
            create(IntelliJPlatformType.IntellijIdea, "2026.2.1")
        }
    }
}
