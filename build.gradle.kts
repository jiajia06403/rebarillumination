import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    java
    kotlin("jvm") version "2.3.0"
    idea
    id("com.gradleup.shadow") version "9.2.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = project.properties["group"]!!

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://jitpack.io") {
        name = "JitPack"
    }
    maven("https://repo.xenondevs.xyz/releases") {
        name = "InvUI"
    }
}

val rebarVersion = project.properties["rebar.version"] as String
val pylonVersion = project.properties["pylon.version"] as String

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("io.github.pylonmc:rebar:$rebarVersion")
    compileOnly("io.github.pylonmc:pylon:$pylonVersion")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:2.3.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("xyz.xenondevs.invui:invui:2.1.0")
    compileOnly("xyz.xenondevs.invui:invui-kotlin:2.1.0")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

kotlin {
    compilerOptions {
        javaParameters = true
        freeCompilerArgs = listOf("-Xwhen-guards")
    }
}

tasks.shadowJar {
    archiveClassifier = ""
}

bukkit {
    name = project.name
    main = project.properties["main-class"] as String
    version = project.version.toString()
    apiVersion = "26.1.2"
    depend = listOf("Rebar", "Pylon")
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
}

tasks.runServer {
    doFirst {
        val pluginsDir = projectDir.resolve("plugins")
        pluginsDir.deleteRecursively()
    }

    downloadPlugins {
        github("pylonmc", "rebar", rebarVersion, "rebar-$rebarVersion.jar")
        github("pylonmc", "pylon", pylonVersion, "pylon-$pylonVersion.jar")
    }

    maxHeapSize = "2G"
    minecraftVersion("26.1.2")
}
