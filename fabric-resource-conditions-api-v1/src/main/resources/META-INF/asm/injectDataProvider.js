var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

function initializeCoreMod() {
    return {
        'injectDataProvider': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.data.DataProvider',
                'methodName': 'm_236069_',
                'methodDesc': '(Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;)V'
            },
            'transformer': function (node) {
                var list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/fabricmc/fabric/api/resource/conditions/v1/ResourceConditions", "CONDITIONS_KEY", "Ljava/lang/String;"));
                list.add(new LdcInsnNode(-100));
                list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "it/unimi/dsi/fastutil/objects/Object2IntOpenHashMap", "put", "(Ljava/lang/Object;I)I"));
                list.add(new InsnNode(Opcodes.POP));
                node.instructions.insert(list);
                ASMAPI.log('DEBUG', 'Injected DataProvider.FIXED_ORDER_FIELDS conditions key priority');
                return node;
            }
        }
    }
}
