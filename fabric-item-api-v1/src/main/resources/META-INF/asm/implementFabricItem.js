var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');

function initializeCoreMod() {
    var yarn = ASMAPI.getSystemPropertyFlag('yarn');
    return {
        'extendFabricItem': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraftforge.common.extensions.IForgeItem'
            },
            'transformer': function (node) {
                node.interfaces.add('net/fabricmc/fabric/api/item/v1/FabricItem');
                return node;
            }
        },
        'implementGetCraftingRemainingItem': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeItem',
                'methodName': 'getCraftingRemainingItem',
                'methodDesc': yarn ? '(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;' : '(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;'
            },
            'transformer': function (node) {
                var insn = ASMAPI.findFirstInstruction(node, Opcodes.NEW);
                if (insn != null && insn.desc === (yarn ? 'net/minecraft/item/ItemStack' : 'net/minecraft/world/item/ItemStack')) {
                    var target = new LabelNode();
                    var callDesc = yarn ? '(Lnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;' : '(Lnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;';
                    var list = ASMAPI.listOf(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, 'net/fabricmc/fabric/impl/item/FabricItemImplHooks', 'getCraftingRemainingItem', callDesc),
                        new InsnNode(Opcodes.DUP),
                        new JumpInsnNode(Opcodes.IFNULL, target),
                        new InsnNode(Opcodes.ARETURN),
                        target
                    );
                    node.instructions.insertBefore(insn, list);
                }
                return node;
            }
        },
        'implementHasCraftingRemainingItem': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeItem',
                'methodName': 'hasCraftingRemainingItem',
                'methodDesc': yarn ? '(Lnet/minecraft/item/ItemStack;)Z' : '(Lnet/minecraft/world/item/ItemStack;)Z'
            },
            'transformer': function (node) {
                var target = new LabelNode();
                var callDesc = yarn ? '(Lnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/item/ItemStack;)Z' : '(Lnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/world/item/ItemStack;)Z';
                var list = ASMAPI.listOf(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, 'net/fabricmc/fabric/impl/item/FabricItemImplHooks', 'hasCraftingRemainingItem', callDesc),
                    new JumpInsnNode(Opcodes.IFEQ, target),
                    new InsnNode(Opcodes.ICONST_1),
                    new InsnNode(Opcodes.IRETURN),
                    target
                );
                node.instructions.insert(list);
                return node;
            }
        },
        'implementGetAttributeModifiers': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeItem',
                'methodName': 'getAttributeModifiers',
                'methodDesc': yarn ? '(Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/item/ItemStack;)Lcom/google/common/collect/Multimap;' : '(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)Lcom/google/common/collect/Multimap;'
            },
            'transformer': function (node) {
                var insn = ASMAPI.findFirstInstruction(node, Opcodes.ARETURN);
                if (insn != null) {
                    var callDesc = yarn ? '(Lcom/google/common/collect/Multimap;Lnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/item/ItemStack;)Lcom/google/common/collect/Multimap;' : '(Lcom/google/common/collect/Multimap;Lnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)Lcom/google/common/collect/Multimap;';
                    var list = ASMAPI.listOf(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, 'net/fabricmc/fabric/impl/item/FabricItemImplHooks', 'getAttributeModifiers', callDesc)
                    );
                    node.instructions.insertBefore(insn, list);
                }
                return node;
            }
        },
        'implementIsCorrectToolForDrops': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeItem',
                'methodName': 'isCorrectToolForDrops',
                'methodDesc': yarn ? '(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/BlockState;)Z' : '(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z'
            },
            'transformer': function (node) {
                var target = new LabelNode();
                var callDesc = yarn ? '(Lnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/BlockState;)Z' : '(Lnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z';
                var list = ASMAPI.listOf(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ALOAD, 2),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, 'net/fabricmc/fabric/impl/item/FabricItemImplHooks', 'isCorrectToolForDrops', callDesc),
                    new JumpInsnNode(Opcodes.IFEQ, target),
                    new InsnNode(Opcodes.ICONST_1),
                    new InsnNode(Opcodes.IRETURN),
                    target
                );
                node.instructions.insert(list);
                return node;
            }
        },
        'implementShouldCauseReequipAnimation': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeItem',
                'methodName': 'shouldCauseReequipAnimation',
                'methodDesc': yarn ? '(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Z)Z' : '(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Z'
            },
            'transformer': function (node) {
                var insn = ASMAPI.findFirstInstruction(node, Opcodes.IFNE);
                if (insn != null) {
                    var callDesc = yarn ? '(Lnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Z)Z' : '(Lnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Z';
                    var list = ASMAPI.listOf(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2),
                        new VarInsnNode(Opcodes.ILOAD, 3),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, 'net/fabricmc/fabric/impl/item/FabricItemImplHooks', 'shouldCauseReequipAnimation', callDesc),
                        new JumpInsnNode(Opcodes.IFEQ, insn.label)
                    );
                    node.instructions.insert(insn, list);
                }
                return node;
            }
        },
        'implementShouldCauseBlockBreakReset': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeItem',
                'methodName': 'shouldCauseBlockBreakReset',
                'methodDesc': yarn ? '(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z' : '(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z'
            },
            'transformer': function (node) {
                for (var i = node.instructions.size() - 1; i >= 0; i--) {
                    var insn = node.instructions.get(i);
                    if (insn.opcode === Opcodes.IRETURN) {
                        var callDesc = yarn ? '(ZLnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z' : '(ZLnet/fabricmc/fabric/api/item/v1/FabricItem;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z';
                        var list = ASMAPI.listOf(
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            new VarInsnNode(Opcodes.ALOAD, 1),
                            new VarInsnNode(Opcodes.ALOAD, 2),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, 'net/fabricmc/fabric/impl/item/FabricItemImplHooks', 'shouldCauseBlockBreakReset', callDesc)
                        );
                        node.instructions.insertBefore(insn, list);
                    }
                }
                return node;
            }
        },
        'implementGetEquipmentSlot': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeItem',
                'methodName': 'getEquipmentSlot',
                'methodDesc': yarn ? '(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/EquipmentSlot;' : '(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EquipmentSlot;'
            },
            'transformer': function (node) {
                var target = new LabelNode();
                var list = ASMAPI.listOf(
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL, yarn ? 'net/minecraft/item/ItemStack' : 'net/minecraft/world/item/ItemStack', ASMAPI.mapMethod('m_41720_'), yarn ? '()Lnet/minecraft/item/Item;' : '()Lnet/minecraft/world/item/Item;'),
                    new TypeInsnNode(Opcodes.CHECKCAST, 'net/fabricmc/fabric/impl/item/ItemExtensions'),
                    new MethodInsnNode(Opcodes.INVOKEINTERFACE, 'net/fabricmc/fabric/impl/item/ItemExtensions', 'fabric_getEquipmentSlotProvider', '()Lnet/fabricmc/fabric/api/item/v1/EquipmentSlotProvider;'),
                    new InsnNode(Opcodes.DUP),
                    new JumpInsnNode(Opcodes.IFNULL, target),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new MethodInsnNode(Opcodes.INVOKEINTERFACE, 'net/fabricmc/fabric/api/item/v1/EquipmentSlotProvider', 'getPreferredEquipmentSlot', yarn ? '(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/EquipmentSlot;' : '(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EquipmentSlot;'),
                    new InsnNode(Opcodes.ARETURN),
                    target
                );
                node.instructions.insert(list);
                return node;
            }
        },
        'implementDamageItem': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeItem',
                'methodName': 'damageItem',
                'methodDesc': yarn ? '(Lnet/minecraft/item/ItemStack;ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)I' : '(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)I'
            },
            'transformer': function (node) {
                var target = new LabelNode();
                var list = ASMAPI.listOf(
                    new VarInsnNode(Opcodes.ALOAD, 0),
                    new TypeInsnNode(Opcodes.CHECKCAST, 'net/fabricmc/fabric/impl/item/ItemExtensions'),
                    new MethodInsnNode(Opcodes.INVOKEINTERFACE, 'net/fabricmc/fabric/impl/item/ItemExtensions', 'fabric_getCustomDamageHandler', '()Lnet/fabricmc/fabric/api/item/v1/CustomDamageHandler;'),
                    new InsnNode(Opcodes.DUP),
                    new JumpInsnNode(Opcodes.IFNULL, target),
                    new VarInsnNode(Opcodes.ALOAD, 1),
                    new VarInsnNode(Opcodes.ILOAD, 2),
                    new VarInsnNode(Opcodes.ALOAD, 3),
                    new VarInsnNode(Opcodes.ALOAD, 4),
                    new MethodInsnNode(Opcodes.INVOKEINTERFACE, 'net/fabricmc/fabric/api/item/v1/CustomDamageHandler', 'damage', yarn ? '(Lnet/minecraft/item/ItemStack;ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)I' : '(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)I'),
                    new InsnNode(Opcodes.IRETURN),
                    target
                );
                node.instructions.insert(list);
                return node;
            }
        }
    }
}
