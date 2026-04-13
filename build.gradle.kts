import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// PLUGINS -- BEGIN
plugins {
    kotlin("jvm") version "2.3.20"
    `java-library`
    jacoco
    id("org.sonarqube") version "7.2.3.7755"
    `maven-publish`
    id("com.diffplug.spotless") version "8.4.0"
}

allprojects {
    apply(plugin = "kotlin")
}
// PLUGINS -- END

// JAVA VERSION & NULLABILITY -- BEGIN
allprojects {
    java.sourceCompatibility = JavaVersion.VERSION_17

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }
}
// JAVA VERSION & NULLABILITY -- END

// SPOTLESS -- BEGIN
allprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        kotlin {
            ktlint()
        }
        kotlinGradle {
            ktlint()
        }
    }

    listOf(tasks.compileJava, tasks.compileKotlin, tasks.compileTestJava, tasks.compileTestKotlin).forEach {
        it.get().mustRunAfter(tasks.spotlessCheck)
    }

    tasks.check {
        dependsOn(tasks.spotlessCheck)
    }
}
// SPOTLESS -- END

// SOURCES -- BEGIN
allprojects {
    java {
        withSourcesJar()
    }
}
// SOURCES -- END

// JAVADOC -- BEGIN
allprojects {
    java {
        withJavadocJar()
    }
}
// JAVADOC -- END

// JACOCO -- BEGIN
allprojects {
    apply(plugin = "jacoco")

    jacoco {
        toolVersion = "0.8.14"
    }

    tasks.jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        dependsOn(tasks.test)
    }

    tasks.build {
        dependsOn(tasks.jacocoTestReport)
    }
}
// JACOCO -- END

// TEST LOGGING -- BEGIN
allprojects {
    tasks.withType<Test> {
        testLogging {
            showStandardStreams = false
            events("skipped", "failed")
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
        addTestListener(
            object : TestListener {
                override fun beforeSuite(suite: TestDescriptor) {}

                override fun afterSuite(
                    suite: TestDescriptor,
                    result: TestResult,
                ) {
                    if (suite.parent == null) {
                        println("------")
                        println(
                            "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} " +
                                "successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)",
                        )
                        println("Tests took: ${result.endTime - result.startTime} ms.")
                        println("------")
                    }
                }

                override fun beforeTest(testDescriptor: TestDescriptor) {}

                override fun afterTest(
                    testDescriptor: TestDescriptor,
                    result: TestResult,
                ) {}
            },
        )
    }
}
// TEST LOGGING -- END

// JUNIT -- BEGIN
allprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
        maxHeapSize = "4g"
    }
}
// JUNIT -- END

// Dependencies -- BEGIN
allprojects {
    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(platform(kotlin("bom")))
        implementation(kotlin("reflect"))
        implementation("org.locationtech.jts:jts-core:1.20.0")
        implementation("org.jgrapht:jgrapht-core:1.5.2")

        testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("org.assertj:assertj-core:3.27.7")
    }
}
// Dependencies -- END

// #####################################################################################################################
