package net.fabricmc.fabric.mixin.loot;

import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;
import net.minecraftforge.resource.ResourcePackLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ResourcePackLoader.class)
public interface ResourcePackLoaderAccessor {
    @Accessor(remap = false)
    static Map<IModFile, PathPackResources> getModResourcePacks() {
        throw new UnsupportedOperationException();
    }
}
