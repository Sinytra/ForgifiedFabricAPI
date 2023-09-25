var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

function initializeCoreMod() {
    var yarn = ASMAPI.getSystemPropertyFlag('yarn');
    var getAppearanceDesc = yarn
        ? '(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;'
        : '(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;';
    return {
        'extendFabricBlock': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraftforge.common.extensions.IForgeBlock'
            },
            'transformer': function (node) {
                node.interfaces.add('net/fabricmc/fabric/api/block/v1/FabricBlock');
                return node;
            }
        },
        'implementGetAppearance': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeBlock',
                'methodName': 'getAppearance',
                'methodDesc': getAppearanceDesc
            },
            'transformer': function (node) {
                var list = ASMAPI.listOf(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new VarInsnNode(Opcodes.ALOAD, 3),
                    new VarInsnNode(Opcodes.ALOAD, 4),
                    new VarInsnNode(Opcodes.ALOAD, 5),
                    new VarInsnNode(Opcodes.ALOAD, 6),
                    new MethodInsnNode(Opcodes.INVOKESPECIAL, 'net/fabricmc/fabric/api/block/v1/FabricBlock', 'getAppearance', getAppearanceDesc, true),
                    new InsnNode(Opcodes.ARETURN)
                );
                node.instructions.insert(list);
                return node;
            }
        },
        
    }
}
