import io.izzel.taboolib.gradle.*

plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "2.0.23"
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
}

taboolib {
    env {
        install(
            Basic,
            Bukkit,
            BukkitNMS,
            BukkitNMSUtil,
            BukkitUI,
            Database,
            MinecraftChat,
            CommandHelper,
            MinecraftEffect,
            BukkitNMSEntityAI,
            BukkitNMSDataSerializer,
            BukkitNMSItemTag,
            BukkitNavigation
        )
    }
    version {
        taboolib = "6.2.3"
    }
}

repositories {
    mavenCentral()
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://repo.tabooproject.org/repository/releases/")
    maven("https://libraries.minecraft.net")
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.21.6-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.108.Final")
    compileOnly("ink.ptms.core:v12103:12103:mapped")
    compileOnly("ink.ptms.core:v12103:12103:universal")
    compileOnly("com.mojang:authlib:3.11.50")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.tabooproject.org/repository/releases")
            credentials {
                username = project.findProperty("taboolibUsername").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            groupId = project.group.toString()
        }
    }
}