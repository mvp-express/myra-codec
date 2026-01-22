plugins {
    `java-library`
    application
    alias(libs.plugins.spotless)
    alias(libs.plugins.spotbugs)
    checkstyle
}

group = "express.mvp.myra.codec"
version = "0.2.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("express.mvp:roray-ffm:0.2.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation(project(":runtime"))
    compileOnly(libs.spotbugs.annotations)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass.set("express.mvp.myra.codec.examples.ExampleApp")
}

// Spotless configuration for Google Java Format
spotless {
    java {
        target("src/**/*.java")
        targetExclude("src/main/java/express/mvp/myra/codec/examples/generated/**")
        googleJavaFormat("1.32.0").aosp().reflowLongStrings()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

val checkstyleDir = file("${rootProject.projectDir}/config/checkstyle")

// Checkstyle configuration - simplified rules for essential code quality
checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configDirectory.set(checkstyleDir)
    configFile = checkstyleDir.resolve("simple_checks.xml")
    isIgnoreFailures = false
    maxWarnings = 0
}

tasks.withType<Checkstyle>().configureEach {
    exclude("**/generated/**")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// SpotBugs configuration
spotbugs {
    toolVersion.set("4.9.8")
    ignoreFailures.set(false)
    excludeFilter.set(file("${rootProject.projectDir}/config/spotbugs/exclude-examples.xml"))
    showStackTraces.set(true)
    showProgress.set(true)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    enabled = true
    excludeFilter.set(file("${rootProject.projectDir}/config/spotbugs/exclude-examples.xml"))
    reports {
        create("html") {
            required.set(true)
            outputLocation.set(file("${layout.buildDirectory.get()}/reports/spotbugs/${name}.html"))
        }
        create("xml") {
            required.set(true)
            outputLocation.set(file("${layout.buildDirectory.get()}/reports/spotbugs/${name}.xml"))
        }
    }
}
