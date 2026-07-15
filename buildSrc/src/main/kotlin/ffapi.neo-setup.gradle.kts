import net.fabricmc.loom.api.LoomGradleExtensionAPI

val versionMc: String by rootProject
val versionForge: String by rootProject
val versionForgifiedFabricLoader: String by rootProject

val loom = extensions.getByType<LoomGradleExtensionAPI>()
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

dependencies {
    "compileOnly"("org.sinytra:forgified-fabric-loader:$versionForgifiedFabricLoader")
    "runtimeOnly"("org.sinytra:forgified-fabric-loader:$versionForgifiedFabricLoader:full")

    "testmodImplementation"(mainSourceSet.output)
    "testmodCompileOnly"("org.sinytra:forgified-fabric-loader:$versionForgifiedFabricLoader")
    "testmodRuntimeOnly"("org.sinytra:forgified-fabric-loader:$versionForgifiedFabricLoader:full")

    if (project.name != "fabric-gametest-api-v1") {
        "testmodImplementation"(project(":fabric-gametest-api-v1", "namedElements"))
    }

    "testImplementation"(testmod.output)
    "testImplementation"("org.mockito:mockito-core:5.4.0")
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.8.1")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.8.1")
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
        enabled = false
    }

    named<ProcessResources>("processResources") {
        filesMatching("assets/*/icon.png") {
            exclude()
            rootProject.file("src/main/resources/assets/fabric/icon.png").copyTo(destinationDir.resolve(path))
        }
    }
}

loom.apply {
    runtimeOnlyLog4j = true

    runs {
        configureEach {
            isIdeConfigGenerated = project.rootProject == project
            property("mixin.debug", "true")
        }

        create("gametest") {
            server()
            name = "Testmod Game Test Server"
            source(testmod)

            // Enable the gametest runner
            property("neoforge.gameTestServer", "true")
        }

        create("testmodClient") {
            client()
            name = "Testmod Client"
            source(testmod)
        }

        create("testmodServer") {
            server()
            name = "Testmod Server"
            source(testmod)
        }
    }
}

