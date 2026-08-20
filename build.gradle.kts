import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.bundling.Zip
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.ComposedJarTask
import org.jetbrains.intellij.platform.gradle.tasks.PatchPluginXmlTask

plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

data class PluginBranding(
    val id: String,
    val name: String,
    val description: String,
    val actionPrefix: String,
    val controllerCategoryName: String,
    val controllerActionName: String,
    val controllerActionDescription: String,
    val templateCategoryName: String,
    val cycleActionDescription: String,
    val incompatiblePluginId: String,
    val archiveBaseName: String
)

val pluginVariantName = providers.gradleProperty("pluginVariant")
    .orElse("angular")
    .get()
    .lowercase()
val pluginBranding = when (pluginVariantName) {
    "angular" -> PluginBranding(
        id = "patrick.kelleter.angular-cli-quick-switch",
        name = "Angular CLI QuickSwitch",
        description = "Switch between related Angular component files with configurable shortcuts. Cycle through controllers, templates, styles, and tests, jump directly to a category, and add custom suffixes.",
        actionPrefix = "QuickSwitch",
        controllerCategoryName = "Controller",
        controllerActionName = "Controllers",
        controllerActionDescription = "Switches directly to and between related controller files",
        templateCategoryName = "Template",
        cycleActionDescription = "Quickly switches between Angular CLI files for one component, e.g. ts, html, css",
        incompatiblePluginId = "patrick.kelleter.file-quick-switch",
        archiveBaseName = "angular-cli-quick-switch"
    )
    "file" -> PluginBranding(
        id = "patrick.kelleter.file-quick-switch",
        name = "File QuickSwitch",
        description = "Switch between related sibling files with configurable shortcuts. Cycle through scripts, markup, styles, and tests, jump directly to a category, and add custom suffixes.",
        actionPrefix = "File QuickSwitch",
        controllerCategoryName = "Scripts",
        controllerActionName = "Scripts",
        controllerActionDescription = "Switches directly to and between related script files",
        templateCategoryName = "Markup",
        cycleActionDescription = "Quickly switches between related files with the same base name, e.g. ts, html, css",
        incompatiblePluginId = "patrick.kelleter.angular-cli-quick-switch",
        archiveBaseName = "file-quick-switch"
    )
    else -> throw GradleException(
        "Unknown pluginVariant '$pluginVariantName'. Supported variants are 'angular' and 'file'."
    )
}

val brandingTokens = mapOf(
    "PLUGIN_ID" to pluginBranding.id,
    "PLUGIN_NAME" to pluginBranding.name,
    "PLUGIN_DESCRIPTION" to pluginBranding.description,
    "ACTION_PREFIX" to pluginBranding.actionPrefix,
    "CONTROLLER_CATEGORY_NAME" to pluginBranding.controllerCategoryName,
    "CONTROLLER_ACTION_NAME" to pluginBranding.controllerActionName,
    "CONTROLLER_ACTION_DESCRIPTION" to pluginBranding.controllerActionDescription,
    "TEMPLATE_CATEGORY_NAME" to pluginBranding.templateCategoryName,
    "CYCLE_ACTION_DESCRIPTION" to pluginBranding.cycleActionDescription,
    "INCOMPATIBLE_PLUGIN_ID" to pluginBranding.incompatiblePluginId
)

group = "patrick.kelleter"
version = "2.0.0"

base {
    archivesName = pluginBranding.archiveBaseName
}

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

val generatedPluginXml = layout.buildDirectory.file("generated/pluginXml/plugin.xml")
val generatePluginXml = tasks.register<Copy>("generatePluginXml") {
    inputs.property("pluginVariant", pluginVariantName)
    inputs.properties(brandingTokens)
    from(layout.projectDirectory.file("src/main/plugin/plugin.xml"))
    into(generatedPluginXml.map { it.asFile.parentFile })
    filteringCharset = "UTF-8"
    filter<ReplaceTokens>("tokens" to brandingTokens)
}

tasks.named<PatchPluginXmlTask>("patchPluginXml") {
    dependsOn(generatePluginXml)
    inputFile = generatedPluginXml
}

tasks.processResources {
    inputs.property("pluginVariant", pluginVariantName)
    inputs.properties(brandingTokens)
    filteringCharset = "UTF-8"
    filter<ReplaceTokens>("tokens" to brandingTokens)
}

tasks.named<Zip>("buildPlugin") {
    archiveBaseName = pluginBranding.archiveBaseName
}

tasks.named<ComposedJarTask>("composedJar") {
    archiveBaseName = pluginBranding.archiveBaseName
}

tasks.check {
    dependsOn(tasks.named("verifyPluginProjectConfiguration"))
}

intellijPlatform {
    buildSearchableOptions = true

    pluginConfiguration {
        id = pluginBranding.id
        name = pluginBranding.name
        description = pluginBranding.description
        changeNotes = """
            <ul>
                <li>Updated for IntelliJ Platform 2024.2 and newer.</li>
                <li>Added a settings page with optional previous-tab closing.</li>
                <li>Added grouped file-type controls and optional spec.ts/spec.js test cycling.</li>
                <li>Added one validated custom file suffix per category.</li>
                <li>Added dedicated shortcuts for tests, ${pluginBranding.controllerActionName.lowercase()}, styles, and markup.</li>
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
