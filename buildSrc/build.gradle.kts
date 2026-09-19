plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.2.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    fun plugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"
    implementation(plugin("me.modmuss50.mod-publish-plugin", "2.2.0"))
    implementation(plugin("org.jetbrains.changelog", "2.2.0"))
}
