package net.fabricmc.fabric.impl.client.model.loading;

import com.mojang.logging.LogUtils;
import cpw.mods.modlauncher.api.INameMappingService;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ModelLoadingMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String TARGET_CLASS = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.CLASS, "net/minecraft/client/resources/model/ModelBakery").replace('/', '.');
    private static final String TARGET_METHOD = "<init>";
    private static final String TARGET_DESC = mapMethodDesc("(Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiling/ProfilerFiller;Ljava/util/Map;Ljava/util/Map;)V");

    private static final String INSN_OWNER = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.CLASS, "net/minecraft/util/profiling/ProfilerFiller");
    private static final String INSN_METHOD = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, "m_6182_");
    private static final String INSN_DESC = "(Ljava/lang/String;)V";

    private boolean appliedModelLoaderHook = false;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (TARGET_CLASS.equals(targetClassName) && !appliedModelLoaderHook) {
            for (MethodNode method : targetClass.methods) {
                if (method.name.equals(TARGET_METHOD) && method.desc.equals(TARGET_DESC)) {
                    for (AbstractInsnNode insn : method.instructions) {
                        if (insn instanceof MethodInsnNode minsn
                                && insn.getOpcode() == Opcodes.INVOKEINTERFACE
                                && minsn.owner.equals(INSN_OWNER)
                                && minsn.name.equals(INSN_METHOD)
                                && minsn.desc.equals(INSN_DESC)
                        ) {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 3));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, targetClassName.replace('.', '/'), "afterMissingModelInit", TARGET_DESC));
                            method.instructions.insertBefore(insn, list);
                            LOGGER.info("Injected afterMissingModelInit into ModelLoader");
                            appliedModelLoaderHook = true;
                            return;
                        }
                    }
                }
            }
        }
    }

    private static String mapMethodDesc(String methodDescriptor) {
        StringBuilder stringBuilder = new StringBuilder("(");
        Arrays.stream(Type.getArgumentTypes(methodDescriptor))
                .map(ModelLoadingMixinPlugin::mapType)
                .forEach(stringBuilder::append);
        Type returnType = Type.getReturnType(methodDescriptor);
        stringBuilder.append(")").append(mapType(returnType));
        return stringBuilder.toString();
    }

    private static Type mapType(Type type) {
        return type.getSort() == Type.OBJECT ? Type.getObjectType(ObfuscationReflectionHelper.remapName(INameMappingService.Domain.CLASS, type.getClassName()).replace('.', '/')) : type;
    }
}
