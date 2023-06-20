package net.fabricmc.fabric.mixin.resource.loader;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.IOException;
import java.io.InputStream;

@Mixin(FallbackResourceManager.class)
public interface FallbackResourceManagerAccessor {
    @Invoker
    static ResourceLocation callGetMetadataLocation(ResourceLocation location) {
        throw new UnsupportedOperationException();
    }
    
    @Invoker
    static ResourceMetadata callParseMetadata(IoSupplier<InputStream> supplier) throws IOException {
        throw new UnsupportedOperationException();
    }
}
