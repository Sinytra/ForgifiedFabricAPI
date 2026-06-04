package org.sinytra.ffapi

import org.gradle.api.file.RegularFileProperty

interface LoomExtension {
    abstract val accessWidenerPath: RegularFileProperty 
}
