/*
 * Source: https://github.com/FabricMC/fabric/blob/88323df583c009a255faa905be94231bafb20b51/gradle/package-info.gradle
 */
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

plugins {
    `java-library`
}

val targetSourceSets = setOf("main", "client")

sourceSets.configureEach {
    if (name in targetSourceSets) {
        // We have to capture the source set name for the lazy string literals,
        // otherwise it'll just be whatever the last source set is in the list.
        val sourceSetName = name
        val taskName = getTaskName("generate", "ImplPackageInfos")
        val task = project.tasks.register<GenerateImplPackageInfos>(taskName) {
            description = "Generates package-info files for $sourceSetName implementation packages."
            // Only apply to default source directory since we also add the generated 
            // sources to the source set.
            header.set(rootProject.file("HEADER"))
            sourceRoot.set(file("src/$sourceSetName/java"))
            outputDir.set(file("src/generated/$sourceSetName"))
        }
        java.srcDir(task)
        val cleanTask = project.tasks.register<Delete>(getTaskName("clean", "ImplPackageInfos")) {
            delete(file("src/generated/$sourceSetName"))
        }
        tasks.named("clean") {
            dependsOn(cleanTask)
        }
    }
}

open class GenerateImplPackageInfos : DefaultTask() {
    companion object {
        val INTERNAL_DIRS = setOf("impl", "mixin")
        const val PACKAGE_INFO = "package-info.java"
    }

    @InputFile
    val header: RegularFileProperty = project.objects.fileProperty()

    @SkipWhenEmpty
    @InputDirectory
    val sourceRoot: DirectoryProperty = project.objects.directoryProperty()

    @OutputDirectory
    val outputDir: DirectoryProperty = project.objects.directoryProperty()

    @TaskAction
    fun execute() {
        val output: Path = outputDir.get().asFile.toPath()
        project.delete(output)
        val headerText = header.get().asFile.readLines().joinToString("\n") // normalize line endings
        val root = sourceRoot.get().asFile.toPath()

        for (dir in INTERNAL_DIRS) {
            val implDir = root.resolve("net/fabricmc/fabric/$dir")
            if (implDir.notExists()) {
                continue
            }

            Files.walk(implDir).filter(Path::isDirectory).forEach { dirPath ->
                val containsJava = dirPath.listDirectoryEntries().any {
                    it.isRegularFile() && it.fileName.toString().endsWith(".java")
                }

                if (containsJava && dirPath.resolve(PACKAGE_INFO).notExists()) {
                    val relativePath = root.relativize(dirPath)
                    val target = output.resolve(relativePath)
                    target.createDirectories()

                    val packageName = relativePath.toString().replace(File.separator, ".")
                    target.resolve(PACKAGE_INFO).writeText(
                        """$headerText
                        |/**
                        |* Implementation code for ${project.name}.
                        |*/
                        |@ApiStatus.Internal
                        |package $packageName;
                        |import org.jetbrains.annotations.ApiStatus;
                        """.trimMargin()
                    )
                }
            }
        }
    }
}