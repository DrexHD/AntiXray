plugins {
    id("multiloader-loader")
    id("net.neoforged.gradle.userdev") version "7.1.39"
}

version = "neoforge-${project.property("mod_version")}+${project.property("minecraft_version")}"

base {
    archivesName = "${project.property("archives_base_name")}"
}


dependencies {
    implementation("net.neoforged:neoforge:${project.property("neoforge_version")}")

    implementation(jarJar("com.moandjiezana.toml:toml4j:${project.property("toml_version")}")!!)
}

accessTransformers {
    file("src/main/resources/META-INF/accesstransformer.cfg")
}

publishMods {
    file.set(tasks.jarJar.get().archiveFile)

    displayName.set("AntiXray ${version.get()}")
    modLoaders.addAll("neoforge")

    curseforge {
        projectId.set("560511")
    }
}