pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/shipth-is/godroid-builder")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                    ?: providers.gradleProperty("gpr.user").orNull
                    ?: System.getProperty("GITHUB_ACTOR")
                    ?: ""
                password = System.getenv("GITHUB_TOKEN")
                    ?: providers.gradleProperty("gpr.token").orNull
                    ?: System.getProperty("GITHUB_TOKEN")
                    ?: ""
            }
        }
    }
}

rootProject.name = "ShipThis Go"
include(":app")
include(":godot_v4_5")
include(":godot_v3_x")
