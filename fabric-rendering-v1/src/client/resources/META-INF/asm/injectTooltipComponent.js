var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

function initializeCoreMod() {
    var yarn = ASMAPI.getSystemPropertyFlag('yarn');
    return {
        'injectTooltipComponent': {
            'target': {
                'type': 'METHOD',
                'class': yarn ? 'net.minecraft.client.gui.tooltip.TooltipComponent' : 'net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent',
                'methodName': 'm_169948_',
                'methodDesc': yarn ? '(Lnet/minecraft/client/item/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;' : '(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;'
            },
            'transformer': function (node) {
                var end = new LabelNode();
                var list = new InsnList();
                list.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/fabricmc/fabric/api/client/rendering/v1/TooltipComponentCallback", "EVENT", "Lnet/fabricmc/fabric/api/event/Event;"));
                list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/fabricmc/fabric/api/event/Event", "invoker", "()Ljava/lang/Object;"));
                list.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/fabricmc/fabric/api/client/rendering/v1/TooltipComponentCallback"));
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/fabricmc/fabric/api/client/rendering/v1/TooltipComponentCallback", "getComponent", yarn ? "(Lnet/minecraft/client/item/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;" : "(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;"));
                list.add(new InsnNode(Opcodes.DUP));
                list.add(new JumpInsnNode(Opcodes.IFNULL, end));
                list.add(new InsnNode(Opcodes.ARETURN));
                list.add(end);
                node.instructions.insert(list);
                ASMAPI.log('DEBUG', 'Injected TooltipComponentCallback hook into ClientTooltipComponent');
                return node;
            }
        }
    }
}
