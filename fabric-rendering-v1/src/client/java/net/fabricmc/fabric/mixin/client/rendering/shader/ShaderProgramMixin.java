/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.client.rendering.shader;

import net.fabricmc.fabric.impl.client.rendering.HackyValueHolder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderProgram.class)
abstract class ShaderProgramMixin {
    // Allow loading shader stages from arbitrary namespaces.
    @Inject(method = "loadShader", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/PathUtil;getPosixFullPath(Ljava/lang/String;)Ljava/lang/String;"))
    private static void captureProgramName(ResourceFactory resourceProvider, ShaderStage.Type type, String name, CallbackInfoReturnable<ShaderStage> cir) {
        if (name.contains(String.valueOf(Identifier.NAMESPACE_SEPARATOR))) {
            HackyValueHolder.fabric_programNamespace = name.substring(0, name.indexOf(Identifier.NAMESPACE_SEPARATOR));
        }
    }

    @Inject(method = "loadShader", at = @At("TAIL"))
    private static void releaseProgramName(ResourceFactory resourceProvider, ShaderStage.Type type, String name, CallbackInfoReturnable<ShaderStage> cir) {
        HackyValueHolder.fabric_programNamespace = null;
    }

    @Mixin(targets = "net/minecraft/client/gl/ShaderProgram$1")
    abstract static class ShaderProgramGlslProcessorMixin {

        @ModifyArg(method = "loadImport", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;getShaderImportLocation(Ljava/lang/String;ZLjava/lang/String;)Lnet/minecraft/util/Identifier;"), index = 2)
        private String modifyImportNamespace(String basePath, boolean isRelative, String name) {
            return isRelative && HackyValueHolder.fabric_programNamespace != null ? HackyValueHolder.fabric_programNamespace + Identifier.NAMESPACE_SEPARATOR + name : name;
        }
    }
}
