import net.neoforged.moddevgradle.dsl.ModDevExtension

val versionMc: String by rootProject
val versionNeoForge: String by rootProject
val versionForgifiedFabricLoader: String by rootProject

val modDev = extensions.getByType<ModDevExtension>()
val sourceSets = extensions.getByType<SourceSetContainer>()

val mainSourceSet = sourceSets.getByName("main")

mainSourceSet.apply {
    java {
        srcDir("src/client/java")
    }
    resources {
        srcDir("src/client/resources")
    }
}

val testmod: SourceSet by sourceSets.creating {
    compileClasspath += mainSourceSet.compileClasspath
    runtimeClasspath += mainSourceSet.runtimeClasspath

    java {
        srcDir("src/testmodClient/java")
    }
    resources {
        srcDir("src/testmodClient/resources")
    }
}

sourceSets.named("test") {
    compileClasspath += testmod.compileClasspath
    runtimeClasspath += testmod.runtimeClasspath
}

dependencies {
    "implementation"("org.sinytra:forgified-fabric-loader:$versionForgifiedFabricLoader")

    "testmodImplementation"(mainSourceSet.output)

    "testImplementation"(testmod.output)
    "testImplementation"("org.mockito:mockito-core:5.4.0")
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.8.1")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")

    if (project.name != "fabric-gametest-api-v1") {
        "testmodImplementation"(project(":fabric-gametest-api-v1"))
    }
}

tasks {
    afterEvaluate {
        named<Jar>("jar") {
            manifest {
                attributes(
                    "Implementation-Version" to project.version
                )
            }
        }
    }

    named<Test>("test") {
        useJUnitPlatform()
        isEnabled = false
    }

    named<ProcessResources>("processResources") {
        filesMatching("assets/*/icon.png") {
            exclude()
            rootProject.file("src/main/resources/assets/fabric/icon.png").copyTo(destinationDir.resolve(path))
        }
    }
}

modDev.apply {
    runs {
        configureEach {
            systemProperty("forge.logging.console.level", "debug")
            systemProperty("mixin.debug", "true")
        }

        create("gametestServer") {
            type = "gameTestServer"
            sourceSet = testmod

            // Enable the gametest runner
            systemProperty("neoforge.enableGameTest", "true")
        }

        create("gametestClient") {
            client()
            sourceSet = testmod

            // Enable the gametest runner
            systemProperty("fabric.client.gametest", "true")
        }

        create("testmodClient") {
            client()
            sourceSet = testmod
        }

        create("testmodServer") {
            server()
            sourceSet = testmod
        }
    }
}

