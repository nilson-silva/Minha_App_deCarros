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
        google()            // <--- OBRIGATÓRIO ESTAR AQUI
        mavenCentral()      // <--- OBRIGATÓRIO ESTAR AQUI
        maven { url = java.net.URI.create("https://jitpack.io") }
    }
}

rootProject.name = "MinhaAppdeCarros"
include(":app")