var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

function initializeCoreMod() {
    return {
        'addFabricKeySortOrders': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.data.DataProvider',
                'methodName': 'm_236069_',
                'methodDesc': '(Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;)V'
            },
            'transformer': function (node) {
                var list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/fabricmc/fabric/impl/datagen/FabricDatagenImpl", "addFabricKeySortOrders", "(Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;)V"))
                for (var insn in node.instructions) {
                    if (insn.opcode === Opcodes.RETURN) {
                        node.instructions.insertBefore(insn, list);
                    }
                }
                ASMAPI.log('DEBUG', 'Injected fabric key sort orders hook into DataProvider.FIXED_ORDER_FIELDS');
                return node;
            }
        }
    }
}