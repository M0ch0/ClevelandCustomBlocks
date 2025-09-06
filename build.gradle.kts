import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}


group = property("group") as String
version = property("version") as String

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://repo.aikar.co/content/groups/aikar/") {
        name = "aikar"
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype"
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines)

    implementation(libs.acf.paper)
    implementation(libs.mccoroutine.bukkit.api)
    implementation(libs.mccoroutine.bukkit.core)
    paperweight.paperDevBundle(libs.versions.paper.api.get())

    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
    implementation(libs.javax.inject)

    detektPlugins(libs.detekt.formatting)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21")
    }
}

tasks.processResources {
    val props = mapOf("name" to rootProject.name, "version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

/*

tasks.withType<Jar> {
    from(layout.buildDirectory.dir("generated/ksp/kotlin"))
}
*/

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")

    val relocateBase = "${project.properties["group"]}.shadow"
    relocate("co.aikar", "$relocateBase.acf")
    relocate("com.github.shynixn.mccoroutine", "$relocateBase.mccoroutine")
    relocate("kotlinx.coroutines", "$relocateBase.kotlinx.coroutines")

    mergeServiceFiles()

    minimize {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib:.*"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*"))
        exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:.*"))
        exclude(dependency("com.github.shynixn.mccoroutine:.*"))
    }
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.check {
    dependsOn(tasks.detekt)
}

detekt {
    toolVersion = libs.versions.detekt.get()

    autoCorrect = true

    buildUponDefaultConfig = true

    allRules = false

    config.setFrom(files("$rootDir/detekt.yml"))
}