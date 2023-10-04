pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://mirrors.tencent.com/nexus/repository/maven-public")
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.bytedance.rhea-trace" -> useModule("com.bytedance.btrace:rhea-gradle-plugin:2.0.3-rc01")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "android-performance"
include(":app")
include(":thread-hook")
include(":memory-hook")
include(":stability-optimize")
include(":startup-optimize")
include(":jank-optimize")
include(":anr-monitor")
