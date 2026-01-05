// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
}

allprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        val ktlintVersion = "1.8.0"

        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt", "**/.idea/**")
            ktlint(ktlintVersion)
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlinGradle {
            target("**/*.gradle.kts")
            targetExclude("**/build/**/*.gradle.kts", "**/.idea/**")
            ktlint(ktlintVersion)
            trimTrailingWhitespace()
            endWithNewline()
        }
        format("xml") {
            target("**/*.xml")
            targetExclude("**/build/**/*.xml", "**/.idea/**")
            trimTrailingWhitespace()
            endWithNewline()
        }
        format("json") {
            target("**/*.json")
            targetExclude("**/build/**/*.json", "**/.idea/**")
            trimTrailingWhitespace()
            endWithNewline()
        }
        format("md") {
            target("**/*.md")
            targetExclude("**/build/**/*.md", "**/.idea/**")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
