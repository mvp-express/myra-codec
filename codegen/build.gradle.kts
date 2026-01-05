import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    application
    alias(libs.plugins.spotless)
    alias(libs.plugins.spotbugs)
    checkstyle
    id("com.gradleup.shadow") version "9.1.0"
    id("com.vanniktech.maven.publish")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

application {
    mainClass.set("express.mvp.myra.codec.codegen.MyraCodegenCli")
}

group = "express.mvp.myra"
version = "0.1.0"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

// Spotless configuration for Google Java Format
spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.32.0").aosp().reflowLongStrings()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

val checkstyleDir = file("${rootProject.projectDir}/config/checkstyle")

// Checkstyle configuration - simplified rules for essential code quality
checkstyle {
    toolVersion = libs.findVersion("checkstyle").get().toString()
    configDirectory.set(checkstyleDir)
    configFile = checkstyleDir.resolve("simple_checks.xml")
    isIgnoreFailures = false
    maxWarnings = 0
}

tasks.withType<Checkstyle>().configureEach {
    exclude("**/module-info.java")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// SpotBugs configuration
spotbugs {
    toolVersion.set("4.9.8")
    ignoreFailures.set(false)
    showStackTraces.set(true)
    showProgress.set(true)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    enabled = true
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

dependencies {
    implementation(project(":schema-core"))
    implementation("express.mvp:roray-ffm:0.1.0")
    implementation("com.palantir.javapoet:javapoet:0.7.0")
    implementation("info.picocli:picocli:4.7.7")

    testImplementation(project(":runtime"))
    testCompileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.12.1")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass.set("express.mvp.myra.codec.codegen.MyraCodegenCli")
}

tasks.named("shadowDistZip") { enabled = false }
tasks.named("shadowDistTar") { enabled = false }
tasks.named("startShadowScripts") { enabled = false }

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "express.mvp.myra.codec.codegen.MyraCodegenCli"
    }
    archiveClassifier.set("all")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "express.mvp.myra.codec.codegen.MyraCodegenCli"
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = group.toString(),
        artifactId = "codec-codegen",
        version = version.toString(),
    )

    pom {
        name.set("myra-codec-codegen")
        description.set("Code generation tool for Myra codec framework.")
        inceptionYear.set("2025")
        url.set("https://github.com/mvp-express/myra-codec")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("mvp-express")
                name.set("MVP Express Team")
                email.set("hi@mvp.express")
            }
        }

        scm {
            url.set("https://github.com/mvp-express/myra-codec")
            connection.set("scm:git:git://github.com/mvp-express/myra-codec.git")
            developerConnection.set("scm:git:ssh://git@github.com/mvp-express/myra-codec.git")
        }
    }
}
