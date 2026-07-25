import net.neoforged.moddevgradle.dsl.ModDevExtension
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.sinytra.ffapi.InterfaceInjection
import org.sinytra.ffapi.LoomExtension
import org.sinytra.ffapi.task.GenerateAccessTransformerTask
import org.sinytra.ffapi.task.GenerateInjectedInterfacesTask
import org.sinytra.ffapi.task.GenerateModMetadataTask

val versionMc: String by rootProject
val versionNeoForge: String by rootProject

val modDev = extensions.getByType<ModDevExtension>()
val loomStub = extensions.create<LoomExtension>("loom")

object Constants {
    const val modMetaBaseTaskName = "ModMetadata"
    const val atBaseTaskName = "AccessTransformer"
    const val modMetadataPath = "META-INF/neoforge.mods.toml"
    const val accessTransformerPath = "META-INF/accesstransformer.cfg"
    const val injectedInterfacesPath = "META-INF/interfaces.json"
    const val generateATTaskName = "generateAccessTransformer"
    const val generateInjectedInterfacesTaskName = "generateInjectedInterfaces"
}

extensions.getByType<SourceSetContainer>().configureEach {
    // We have to capture the source set name for the lazy string literals,
    // otherwise it'll just be whatever the last source set is in the list.
    val sourceSetName = name
    val resourceRoots = resources.srcDirs

    val modMetaTaskName = getTaskName("generate", Constants.modMetaBaseTaskName)
    val generateModMeta = tasks.register(modMetaTaskName, GenerateModMetadataTask::class.java) {
        group = "sinytra"
        description = "Generates neoforge.mods.toml for $sourceSetName fabric mod."

        // Only apply to default source directory since we also add the generated
        // sources to the source set.
        sourceRoots.from(resourceRoots)
        outputFile = file("src/generated/$sourceSetName/resources/${Constants.modMetadataPath}")
        forgeVersionString = versionNeoForge
        minecraftVersionString = versionMc
    }

    if (sourceSetName != "main") {
        resources.srcDirs(files("src/generated/$sourceSetName/resources").builtBy(generateModMeta))
    }

    val cleanTask = tasks.register(getTaskName("clean", Constants.modMetaBaseTaskName), Delete::class.java) {
        group = "sinytra"
        delete(file("src/generated/$sourceSetName/resources"))
    }
    tasks.named("clean") {
        dependsOn(cleanTask)
    }
    tasks.named("generate") {
        dependsOn(generateModMeta)
    }
    tasks.named<Jar>("jar") {
        exclude("fabric.mod.json")
    }
}

extensions.getByType<SourceSetContainer>().named("main").configure {
    val generatedAtFile = file("src/generated/main/resources/${Constants.accessTransformerPath}")
    val generateAccessTransformer = tasks.register(Constants.generateATTaskName, GenerateAccessTransformerTask::class.java) {
        group = "sinytra"
        description = "Generates accesstransformer.cfg for fabric mod."

        outputFile = generatedAtFile
        classTweaker = provider { loomStub.accessWidenerPath.orNull }
    }

    val generateInjectedInterfaces = tasks.register(Constants.generateInjectedInterfacesTaskName, GenerateInjectedInterfacesTask::class) {
        group = "sinytra"

        outputFile = file("src/generated/main/resources/${Constants.injectedInterfacesPath}")
        classTweaker = provider { loomStub.accessWidenerPath.orNull }
    }

    val modMetaTaskName = getTaskName("generate", Constants.modMetaBaseTaskName)
    resources.srcDir(
        files("src/generated/main/resources")
            .builtBy(generateAccessTransformer, generateInjectedInterfaces, modMetaTaskName)
    )

    tasks.named("generate") {
        dependsOn(generateAccessTransformer, generateInjectedInterfaces)
    }
    tasks.named("copyAccessTransformersPublications") {
        dependsOn(generateAccessTransformer)
    }
    tasks.named("copyInterfaceInjectionDataPublications") {
        dependsOn(generateInjectedInterfaces)
    }
}

afterEvaluate {
    loomStub.accessWidenerPath.orNull?.also { value ->
        tasks.withType<Jar> {
            exclude(loomStub.accessWidenerPath.get().asFile.name)
        }

        val atFile = tasks.named<GenerateAccessTransformerTask>(Constants.generateATTaskName).flatMap { it.outputFile }
        modDev.accessTransformers.from(atFile)
        modDev.accessTransformers.publish(atFile)

        val file = value.asFile
        val fileOutputDir = file("src/generated/main/resources")

        val hasInterfaces = file.bufferedReader().use(InterfaceInjection::hasInjectedInterfaces)
        if (hasInterfaces) {
            val neoForge = the<NeoForgeExtension>()
            val generatedFile = fileOutputDir.resolve(Constants.injectedInterfacesPath)

            neoForge.interfaceInjectionData.from(
                files(generatedFile).builtBy(Constants.generateInjectedInterfacesTaskName)
            )
            neoForge.interfaceInjectionData.publish(generatedFile)
        }
    }
}


