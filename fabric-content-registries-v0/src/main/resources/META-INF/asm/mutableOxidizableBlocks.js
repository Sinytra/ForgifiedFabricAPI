var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

function initializeCoreMod() {
    var yarn = ASMAPI.getSystemPropertyFlag('yarn');
    return {
        'mutableOxidizableBlocks': {
            'target': {
                'type': 'METHOD',
                'class': yarn ? 'net.minecraft.block.Oxidizable' : 'net.minecraft.world.level.block.WeatheringCopper',
                'methodName': 'm_154909_',
                'methodDesc': '()Lcom/google/common/collect/BiMap;'
            },
            'transformer': function (node) {
                var returnInsn = ASMAPI.findFirstInstruction(node, Opcodes.ARETURN);
                node.instructions.insertBefore(returnInsn, new MethodInsnNode(Opcodes.INVOKESTATIC, 'com/google/common/collect/HashBiMap', 'create', '(Ljava/util/Map;)Lcom/google/common/collect/HashBiMap;', false));
                ASMAPI.log('DEBUG', 'Made net.minecraft.block.Oxidizable.OXIDATION_LEVEL_INCREASES mutable');
                return node;
            }
        }
    }
}
