var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

function initializeCoreMod() {
    var yarn = ASMAPI.getSystemPropertyFlag('yarn');
    var className = yarn ? 'net.minecraft.client.render.model.ModelLoader' : 'net.minecraft.client.resources.model.ModelBakery';
    var desc = yarn ? '(Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;Ljava/util/Map;Ljava/util/Map;)V' : '(Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiling/ProfilerFiller;Ljava/util/Map;Ljava/util/Map;)V'
    return {
        'afterMissingModelInit': {
            'target': {
                'type': 'METHOD',
                'class': className,
                'methodName': '<init>',
                'methodDesc': desc
            },
            'transformer': function (node) {
                var wantedOwner = yarn ? 'net/minecraft/util/profiler/Profiler' : 'net/minecraft/util/profiling/ProfilerFiller';
                var wantedName = ASMAPI.mapMethod('m_6182_');
                for (var i = 0; i < node.instructions.size(); i++) {
                    var insn = node.instructions.get(i);
                    if (insn.opcode === Opcodes.INVOKEINTERFACE && insn.owner == wantedOwner && insn.name == wantedName && insn.desc == '(Ljava/lang/String;)V') {
                        var list = new InsnList();
                        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
                        list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, className.replaceAll('\\.', '/'), 'afterMissingModelInit', desc));
                        node.instructions.insertBefore(insn, list);
                        ASMAPI.log('DEBUG', 'Injected afterMissingModelInit into ModelLoader');
                        break;
                    }
                }
                return node;
            }
        }
    }
}
