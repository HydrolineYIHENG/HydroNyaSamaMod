pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        mavenCentral()
    }
    plugins {
        id("dev.architectury.loom") version "1.6.422"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenLocal()
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        mavenCentral()
    }
}

rootProject.name = "HydroNyaSama"
include(
    "common",
    "fabric-1.16.5",
    "fabric-1.18.2",
    "fabric-1.20.1",
    "forge-1.16.5",
    "forge-1.18.2",
    "forge-1.20.1"
)
