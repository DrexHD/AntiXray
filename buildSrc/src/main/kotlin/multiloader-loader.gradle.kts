import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.ChangelogPluginExtension

plugins {
	id("java")
	id("idea")
    id("multiloader-common")
    id("me.modmuss50.mod-publish-plugin")
    id("org.jetbrains.changelog")
}

val commonJava: Configuration by configurations.creating {
    isCanBeResolved = true
}
val commonResources: Configuration by configurations.creating {
    isCanBeResolved = true
}

dependencies {
    val commonPath = ":common"
	compileOnly(project(path = commonPath))
    commonJava(project(path = commonPath, configuration = "commonJava"))
    commonResources(project(path = commonPath, configuration = "commonResources"))
}

tasks {
    compileJava {
        dependsOn(commonJava)
        source(commonJava)
    }

    processResources {
        dependsOn(commonResources)
        from(commonResources)
    }

    publishMods {
        type.set(STABLE)
        displayName.set("AntiXray ${version.get()}")
        changelog.set(provider { fetchChangelog() })

        curseforge {
            accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
            minecraftVersions.addAll((project.property("curseforge_minecraft_versions") as String).split(", "))
	    client = true
	    server = true
        }

        modrinth {
            accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
            projectId.set("sml2FMaA")
            minecraftVersions.addAll((project.property("modrinth_minecraft_versions") as String).split(", "))
        }

        github {
            accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
            repository.set(providers.environmentVariable("GITHUB_REPOSITORY").orElse("DrexHD/AntiXray"))
            commitish.set(providers.environmentVariable("GITHUB_REF_NAME").orElse("main"))
        }
    }
}

changelog {
    path.set(rootProject.file("CHANGELOG.md").path)
}

fun fetchChangelog(): String {
    val log = project.extensions.getByType<ChangelogPluginExtension>()
    val modVersion = findProperty("mod_version")!!.toString()
    return if (log.has(modVersion)) {
        log.renderItem(
            log.get(modVersion).withHeader(false),
            Changelog.OutputType.MARKDOWN
        )
    } else {
        ""
    }
}
