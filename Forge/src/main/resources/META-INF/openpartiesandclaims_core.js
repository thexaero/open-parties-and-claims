var Opcodes=Java.type('org.objectweb.asm.Opcodes')
var InsnList=Java.type('org.objectweb.asm.tree.InsnList')
var VarInsnNode=Java.type('org.objectweb.asm.tree.VarInsnNode')
var MethodInsnNode=Java.type('org.objectweb.asm.tree.MethodInsnNode')
var MethodNode=Java.type('org.objectweb.asm.tree.MethodNode')
var InsnNode=Java.type('org.objectweb.asm.tree.InsnNode')
var FieldInsnNode=Java.type('org.objectweb.asm.tree.FieldInsnNode')
var LabelNode=Java.type('org.objectweb.asm.tree.LabelNode')
var LocalVariableNode=Java.type('org.objectweb.asm.tree.LocalVariableNode')
var Label=Java.type('org.objectweb.asm.Label')
var JumpInsnNode=Java.type('org.objectweb.asm.tree.JumpInsnNode')
var FieldNode=Java.type('org.objectweb.asm.tree.FieldNode')

var levelClass = 'net/minecraft/world/level/Level'
var getBlockStateName = 'getBlockState'
var getBlockStateNameObf = 'm_8055_'
var getBlockStateDesc = '(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;'

function addCustomGetter(classNode, fieldName, fieldDesc, methodName){
	var methods = classNode.methods
	var getterNode = new MethodNode(Opcodes.ACC_PUBLIC, methodName, "()" + fieldDesc, null, null)
	var labelNode1 = new LabelNode()
	var labelNode2 = new LabelNode()
	var instructions = getterNode.instructions
	instructions.add(labelNode1)
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
	instructions.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, fieldName, fieldDesc))
	instructions.add(new InsnNode(Opcodes.ARETURN))
	instructions.add(labelNode2)
	getterNode.localVariables.add(new LocalVariableNode("this", "L" + classNode.name + ";", null, labelNode1, labelNode2, 0))
	getterNode.maxStack = 1
	getterNode.maxLocals = 1
	methods.add(getterNode)
}

function addGetter(classNode, fieldName, fieldDesc){
	addCustomGetter(classNode, fieldName, fieldDesc, "get" + (fieldName.charAt(0) + "").toUpperCase() + fieldName.substring(1))
}

function addSetter(classNode, fieldName, fieldDesc){
	var methods = classNode.methods
	var setterNode = new MethodNode(Opcodes.ACC_PUBLIC, "set" + (fieldName.charAt(0) + "").toUpperCase() + fieldName.substring(1), "(" + fieldDesc +  ")V", null, null)
	var labelNode1 = new LabelNode()
	var labelNode2 = new LabelNode()
	var instructions = setterNode.instructions
	instructions.add(labelNode1)
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
	instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, classNode.name, fieldName, fieldDesc))
	instructions.add(new InsnNode(Opcodes.RETURN))
	instructions.add(labelNode2)
	setterNode.localVariables.add(new LocalVariableNode("this", "L" + classNode.name + ";", null, labelNode1, labelNode2, 0))
	setterNode.localVariables.add(new LocalVariableNode("value", fieldDesc, null, labelNode1, labelNode2, 1))
	setterNode.maxStack = 2
	setterNode.maxLocals = 2
	methods.add(setterNode)
}

function clientPacketRedirectTransformCustom(methodNode, methodInsnNode, localVariable){
	var instructions = methodNode.instructions
	var patchList = new InsnList()
	patchList.add(new VarInsnNode(Opcodes.ALOAD, localVariable))
	patchList.add(methodInsnNode)
	for(var i = 0; i < instructions.size(); i++) {
		var insn = instructions.get(i);
		if(insn.getOpcode() == Opcodes.INVOKESTATIC) {
			if(insn.owner.equals("net/minecraft/network/protocol/PacketUtils") && (insn.name.equals("ensureRunningOnSameThread") || insn.name.equals("m_131363_"))) {
				instructions.insert(insn, patchList);
				break;
			}
		}
	}
}

function insertBeforeReturn2(methodNode, patchListGetter){
	var instructions = methodNode.instructions
	for(var i = 0; i < instructions.size(); i++) {
		var insn = instructions.get(i);
		if(insn.getOpcode() >= 172 && insn.getOpcode() <= 177){
		    var toInsert = patchListGetter()
        	var patchSize = toInsert.size()
			instructions.insertBefore(insn, toInsert);
			i += patchSize
		}
	}
}

function insertBeforeReturn(methodNode, patchList){
    var patchListGetter = function() {return patchList}
    insertBeforeReturn2(methodNode, patchListGetter)
}

function insertOnInvoke2(methodNode, patchListGetter, before, invokeOwner, invokeName, invokeNameObf, invokeDesc, firstOnly){
	var instructions = methodNode.instructions
	var isObfuscated = false
	for(var i = 0; i < instructions.size(); i++) {
		var insn = instructions.get(i);
		if(insn.getOpcode() >= 182 && insn.getOpcode() <= 185) {
			if(insn.owner.equals(invokeOwner) && (insn.name.equals(invokeName) || insn.name.equals(invokeNameObf)) && insn.desc.equals(invokeDesc)) {
				if(insn.name.equals(invokeNameObf))
				    isObfuscated = true
				var toInsert = patchListGetter()
				var patchSize = toInsert.size()
				if(before)
				    instructions.insertBefore(insn, toInsert);
                else
                	instructions.insert(insn, toInsert);
                i += patchSize
                if(firstOnly)
				    break
			}
		}
	}
	return isObfuscated
}

function insertOnInvoke(methodNode, patchList, before, invokeOwner, invokeName, invokeNameObf, invokeDesc){
    var patchListGetter = function() {return patchList}
    return insertOnInvoke2(methodNode, patchListGetter, before, invokeOwner, invokeName, invokeNameObf, invokeDesc, true)
}

function clientPacketRedirectTransform(methodNode, methodInsnNode){
	clientPacketRedirectTransformCustom(methodNode, methodInsnNode, 1)
}

function insertCreateModBlockPosArgumentCapture(methodNode, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc){
    var insnToInsertBeforeGetter = function() {
        var insnToInsertBefore = new InsnList()
        insnToInsertBefore.add(new InsnNode(Opcodes.DUP))//store the target pos in a field
        insnToInsertBefore.add(new FieldInsnNode(Opcodes.PUTSTATIC, 'xaero/pac/common/server/core/ServerCore', 'CAPTURED_TARGET_POS', 'Lnet/minecraft/core/BlockPos;'))
        return insnToInsertBefore
    }
    return insertOnInvoke2(methodNode, insnToInsertBeforeGetter, true/*before*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
}

function getCreateModBlockBreakHandlerInsn(){
    return new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'replaceBlockFetchOnCreateModBreak', '(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;')
}

function transformCreateBreakerMovementBehaviour(methodNode){
    insertCreateModBlockPosArgumentCapture(methodNode, levelClass, getBlockStateName, getBlockStateNameObf, getBlockStateDesc)

    var insnToInsertGetter = function() {
        var insnToInsert = new InsnList()
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))//movement context
        insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/components/structureMovement/MovementContext', 'world', 'Lnet/minecraft/world/level/Level;'))
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
        insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/components/structureMovement/MovementContext', 'contraption', 'Lcom/simibubi/create/content/contraptions/components/structureMovement/Contraption;'))
        insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/components/structureMovement/Contraption', 'anchor', 'Lnet/minecraft/core/BlockPos;'))
        insnToInsert.add(getCreateModBlockBreakHandlerInsn())
        return insnToInsert
    }
    insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, levelClass, getBlockStateName, getBlockStateNameObf, getBlockStateDesc, false)
    return methodNode
}

function transformCreateSymmetryWandApply(methodNode){
    var invokeTargetClass = 'com/simibubi/create/content/curiosities/symmetry/mirror/SymmetryMirror'
    var invokeTargetName = 'process'
    var invokeTargetNameObf = invokeTargetName
    var invokeTargetDesc = '(Ljava/util/Map;)V'

    var insnToInsertBeforeGetter = function() {
        var insnToInsertBefore = new InsnList()
        insnToInsertBefore.add(new InsnNode(Opcodes.DUP))//store the map argument in a field
        insnToInsertBefore.add(new FieldInsnNode(Opcodes.PUTSTATIC, 'xaero/pac/common/server/core/ServerCore', 'CAPTURED_POS_STATE_MAP', 'Ljava/util/Map;'))
        return insnToInsertBefore
    }
    insertOnInvoke2(methodNode, insnToInsertBeforeGetter, true/*before*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, true)

    var insnToInsert = new InsnList()
    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'onCreateModSymmetryProcessed', '(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;)V'))
    insertOnInvoke(methodNode, insnToInsert, false/*after*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc)

    return methodNode
}

function transformCreateCollideEntities(methodNode){
    var invokeTargetClass = 'net/minecraft/world/level/Level'
    var invokeTargetName = 'getEntitiesOfClass'
    var invokeTargetNameObf = 'm_6443_'
    var invokeTargetDesc = '(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;'

    var insnToInsertGetter = function() {
        var insnToInsert = new InsnList()
        insnToInsert.add(new InsnNode(Opcodes.DUP))
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
        insnToInsert.add(new InsnNode(Opcodes.DUP))
        insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/components/structureMovement/AbstractContraptionEntity', 'contraption', 'Lcom/simibubi/create/content/contraptions/components/structureMovement/Contraption;'))
        insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/components/structureMovement/Contraption', 'anchor', 'Lnet/minecraft/core/BlockPos;'))
        insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'onCreateCollideEntities', '(Ljava/util/List;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)V'))
        return insnToInsert
    }
    insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
    return methodNode
}

function transformCreateMechArmSearch(methodNode, listFieldName) {
    var MY_LABEL = new LabelNode(new Label())
    var insnToInsert = new InsnList()
    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
    insnToInsert.add(new InsnNode(Opcodes.DUP))
    insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/logistics/block/mechanicalArm/ArmTileEntity', listFieldName, 'Ljava/util/List;'))
    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'isCreateMechanicalArmValid', '(Lnet/minecraft/world/level/block/entity/BlockEntity;Ljava/util/List;)Z'))
    insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
    insnToInsert.add(new InsnNode(Opcodes.RETURN))
    insnToInsert.add(MY_LABEL)
    methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
}

function transformForEntitiesPushBlock(methodNode, includeClassFiltered, includeNonClassFiltered){
    var invokeTargetClass = 'net/minecraft/world/level/Level'
    var insnToInsertGetter = function() {
        var insnToInsert = new InsnList()
        insnToInsert.add(new InsnNode(Opcodes.DUP))
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
        insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'onEntitiesPushBlock', '(Ljava/util/List;Lnet/minecraft/world/level/block/Block;)V'))
        return insnToInsert
    }
    if(includeClassFiltered){
        var invokeTargetName = 'getEntitiesOfClass'
        var invokeTargetNameObf = 'm_45976_'
        var invokeTargetDesc = '(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;'
        insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
    }
    if(includeNonClassFiltered){
        var invokeTargetName = 'getEntities'
        var invokeTargetNameObf = 'm_45933_'
        var invokeTargetDesc = '(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;'
        insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
    }
    return methodNode
}

function initializeCoreMod() {
	return {
		'xaero_pac_minecraftserverclass': {
			'target' : {
				'type' : 'CLASS',
				'name' : 'net.minecraft.server.MinecraftServer'
			},
			'transformer' : function(classNode){
				var fields = classNode.fields
				classNode.interfaces.add("xaero/pac/common/server/IOpenPACMinecraftServer")
				fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "xaero_OPAC_ServerData", "Lxaero/pac/common/server/IServerDataAPI;", null, null))
				addGetter(classNode, "xaero_OPAC_ServerData", "Lxaero/pac/common/server/IServerDataAPI;")
				addSetter(classNode, "xaero_OPAC_ServerData", "Lxaero/pac/common/server/IServerDataAPI;")
				
				return classNode
			}
		},
		'xaero_pac_serverplayerclass': {
			'target' : {
				'type' : 'CLASS',
				'name' : 'net.minecraft.server.level.ServerPlayer'
			},
			'transformer' : function(classNode){
				var fields = classNode.fields
				classNode.interfaces.add("xaero/pac/common/server/player/data/IOpenPACServerPlayer")
				fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "xaero_OPAC_PlayerData", "Lxaero/pac/common/server/player/data/api/ServerPlayerDataAPI;", null, null))
				addGetter(classNode, "xaero_OPAC_PlayerData", "Lxaero/pac/common/server/player/data/api/ServerPlayerDataAPI;")
				addSetter(classNode, "xaero_OPAC_PlayerData", "Lxaero/pac/common/server/player/data/api/ServerPlayerDataAPI;")
				
				return classNode
			}
		},
		'xaero_pac_integratedserver_tickpaused': {
			'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.client.server.IntegratedServer',
                'methodName': 'm_174968_',
                'methodDesc' : '()V'
			},
			'transformer' : function(methodNode){
				var instructions = methodNode.instructions
				var patchList = new InsnList()
				patchList.add(new VarInsnNode(Opcodes.ALOAD, 0))
				patchList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 
						"onServerTickStart", "(Lnet/minecraft/server/MinecraftServer;)V"))
				instructions.insert(instructions.get(0), patchList)
				return methodNode
			}
		},
		'xaero_pac_abstractclientplayerentity_getlocationcape': {
			'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.client.player.AbstractClientPlayer',
                'methodName': 'm_108561_',
                'methodDesc' : '()Lnet/minecraft/resources/ResourceLocation;'
			},
			'transformer' : function(methodNode){
				var MY_LABEL = new LabelNode(new Label())
				methodNode.maxStack += 1
				var insnToInsert = new InsnList()
				insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
				insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xaero/pac/client/core/ClientCore", "getPlayerCape", "(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;"))
				insnToInsert.add(new InsnNode(Opcodes.DUP))
				insnToInsert.add(new JumpInsnNode(Opcodes.IFNULL, MY_LABEL))
				insnToInsert.add(new InsnNode(Opcodes.ARETURN))
				insnToInsert.add(MY_LABEL)
				insnToInsert.add(new InsnNode(Opcodes.POP))
				methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
				return methodNode
			}
		},
		'xaero_pac_playerentity_iswearing': {
			'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.player.Player',
                'methodName': 'm_36170_',
                'methodDesc' : '(Lnet/minecraft/world/entity/player/PlayerModelPart;)Z'
			},
			'transformer' : function(methodNode){
				var MY_LABEL = new LabelNode(new Label())
				var insnToInsert = new InsnList()
				insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
				insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
				insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xaero/pac/client/core/ClientCore", "isWearing", "(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/player/PlayerModelPart;)Ljava/lang/Boolean;"))
				insnToInsert.add(new InsnNode(Opcodes.DUP))
				insnToInsert.add(new JumpInsnNode(Opcodes.IFNULL, MY_LABEL))
				insnToInsert.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z"))
				insnToInsert.add(new InsnNode(Opcodes.IRETURN))
				insnToInsert.add(MY_LABEL)
				insnToInsert.add(new InsnNode(Opcodes.POP))
				methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
				return methodNode
			}
		},
        'xaero_pac_clientplaynethandler_handleinitializeborder': {
            'target' : {
                 'type': 'METHOD',
                 'class': 'net.minecraft.client.multiplayer.ClientPacketListener',
                 'methodName': 'm_142237_',
                 'methodDesc' : '(Lnet/minecraft/network/protocol/game/ClientboundInitializeBorderPacket;)V'
            },
            'transformer' : function(methodNode){
                clientPacketRedirectTransform(methodNode, new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/client/core/ClientCore',
                        "onInitializeWorldBorder", "(Lnet/minecraft/network/protocol/game/ClientboundInitializeBorderPacket;)V"))
                return methodNode
            }
        },
        'xaero_pac_playerlist_sendworldinfo': {
            'target' : {
                 'type': 'METHOD',
                 'class': 'net.minecraft.server.players.PlayerList',
                 'methodName': 'm_11229_',
                 'methodDesc' : '(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V'
            },
            'transformer' : function(methodNode){
                var instructions = methodNode.instructions
                var patchList = new InsnList()
                patchList.add(new VarInsnNode(Opcodes.ALOAD, 1))
                patchList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore',
                        "onServerWorldInfo", "(Lnet/minecraft/world/entity/player/Player;)V"))
                instructions.insert(instructions.get(0), patchList)
                return methodNode
            }
        },
        'xaero_pac_livingentity_addeffect': {
            'target' : {
                 'type': 'METHOD',
                 'class': 'net.minecraft.world.entity.LivingEntity',
                 'methodName': 'm_147207_',
                 'methodDesc' : '(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z'
            },
            'transformer' : function(methodNode){
				var MY_LABEL = new LabelNode(new Label())
				var insnToInsert = new InsnList()
				insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
				insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
				insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
				insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', "canAddLivingEntityEffect", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
				insnToInsert.add(new InsnNode(Opcodes.DUP))
				insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
				insnToInsert.add(new InsnNode(Opcodes.IRETURN))
				insnToInsert.add(MY_LABEL)
				insnToInsert.add(new InsnNode(Opcodes.POP))
				methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_fireblock_trycatchfire': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.FireBlock',
                'methodName': 'tryCatchFire',
                'methodDesc' : '(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILjava/util/Random;ILnet/minecraft/core/Direction;)V'
            },
            'transformer' : function(methodNode){
                var MY_LABEL = new LabelNode(new Label())
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', "canSpreadFire", "(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
                insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
                insnToInsert.add(new InsnNode(Opcodes.RETURN))
                insnToInsert.add(MY_LABEL)
                methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_fireblock_getfireodds': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.FireBlock',
                'methodName': 'm_53441_',
                'methodDesc' : '(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)I'
            },
            'transformer' : function(methodNode){
                var MY_LABEL = new LabelNode(new Label())
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', "canSpreadFire", "(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
                insnToInsert.add(new InsnNode(Opcodes.DUP))
                insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
                insnToInsert.add(new InsnNode(Opcodes.IRETURN))
                insnToInsert.add(MY_LABEL)
                insnToInsert.add(new InsnNode(Opcodes.POP))
                methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_player_mayuseitemat': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.player.Player',
                'methodName': 'm_36204_',
                'methodDesc' : '(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;)Z'
            },
            'transformer' : function(methodNode){
                var MY_LABEL = new LabelNode(new Label())
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 3))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'mayUseItemAt', '(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/ItemStack;)Z'))
                insnToInsert.add(new InsnNode(Opcodes.DUP))
                insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
                insnToInsert.add(new InsnNode(Opcodes.IRETURN))
                insnToInsert.add(MY_LABEL)
                insnToInsert.add(new InsnNode(Opcodes.POP))
                methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_flowingfluid_canpassthrough': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.material.FlowingFluid',
                'methodName': 'm_75963_',
                'methodDesc' : '(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Z'
            },
            'transformer' : function(methodNode){
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 3))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 6))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'replaceFluidCanPassThrough', '(ZLnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Z'))
                insertBeforeReturn(methodNode, insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_dispenserblock_dispensefrom': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.DispenserBlock',
                'methodName': 'm_5824_',
                'methodDesc' : '(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V'
            },
            'transformer' : function(methodNode){
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'replaceDispenseBehavior', '(Lnet/minecraft/core/dispenser/DispenseItemBehavior;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/dispenser/DispenseItemBehavior;'))
                insertOnInvoke(methodNode, insnToInsert, false/*after*/, 'net/minecraft/world/level/block/DispenserBlock', 'getDispenseMethod', 'm_7216_', '(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/core/dispenser/DispenseItemBehavior;')
                return methodNode
            }
        },
        'xaero_pac_pistonbaseblock_moveblocks': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.piston.PistonBaseBlock',
                'methodName': 'm_60181_',
                'methodDesc' : '(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z'
            },
            'transformer' : function(methodNode){
                var MY_LABEL = new LabelNode(new Label())
                var insnToInsert = new InsnList()
                insnToInsert.add(new InsnNode(Opcodes.DUP))//the PistonStructureResolver
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 3))
                insnToInsert.add(new VarInsnNode(Opcodes.ILOAD, 4))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'canPistonPush', '(Lnet/minecraft/world/level/block/piston/PistonStructureResolver;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z'))
                insnToInsert.add(new InsnNode(Opcodes.DUP))
                insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
                insnToInsert.add(new InsnNode(Opcodes.IRETURN))
                insnToInsert.add(MY_LABEL)
                insnToInsert.add(new InsnNode(Opcodes.POP))
                insertOnInvoke(methodNode, insnToInsert, true/*before*/, 'net/minecraft/world/level/block/piston/PistonStructureResolver', 'getToPush', 'm_60436_', '()Ljava/util/List;')
                return methodNode
            }
        },
        'xaero_pac_create_contraption_movementallowed': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.contraptions.components.structureMovement.Contraption',
                'methodName': 'movementAllowed',
                'methodDesc' : '(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z'
            },
            'transformer' : function(methodNode){
                var MY_LABEL = new LabelNode(new Label())
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 3))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/components/structureMovement/Contraption', 'anchor', 'Lnet/minecraft/core/BlockPos;'))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'isCreateModAllowed', '(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Z'))
                insnToInsert.add(new InsnNode(Opcodes.DUP))
                insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
                insnToInsert.add(new InsnNode(Opcodes.IRETURN))
                insnToInsert.add(MY_LABEL)
                insnToInsert.add(new InsnNode(Opcodes.POP))
                methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_create_contraption_addblockstoworld': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.contraptions.components.structureMovement.Contraption',
                'methodName': 'addBlocksToWorld',
                'methodDesc' : '(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/content/contraptions/components/structureMovement/StructureTransform;)V'
            },
            'transformer' : function(methodNode){
                insertCreateModBlockPosArgumentCapture(methodNode, levelClass, getBlockStateName, getBlockStateNameObf, getBlockStateDesc)

                var insnToInsertGetter = function() {
                    var insnToInsert = new InsnList()
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                    insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/components/structureMovement/Contraption', 'anchor', 'Lnet/minecraft/core/BlockPos;'))
                    insnToInsert.add(getCreateModBlockBreakHandlerInsn())
                    return insnToInsert
                }
                insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, levelClass, getBlockStateName, getBlockStateNameObf, getBlockStateDesc, false)
                return methodNode
            }
        },
        'xaero_pac_create_blockbreakingkinetictileentity_tick': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.contraptions.components.actors.BlockBreakingKineticTileEntity',
                'methodName': 'tick',
                'methodDesc' : '()V'
            },
            'transformer' : function(methodNode){
                var isObfuscated = insertCreateModBlockPosArgumentCapture(methodNode, levelClass, getBlockStateName, getBlockStateNameObf, getBlockStateDesc)

                var insnToInsertGetter = function() {
                    var insnToInsert = new InsnList()
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                    insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'net/minecraft/world/level/block/entity/BlockEntity', isObfuscated? 'f_58857_' : 'level', 'Lnet/minecraft/world/level/Level;'))
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                    insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/base/KineticTileEntity', 'source', 'Lnet/minecraft/core/BlockPos;'))
                    insnToInsert.add(getCreateModBlockBreakHandlerInsn())
                    return insnToInsert
                }
                insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, levelClass, getBlockStateName, getBlockStateNameObf, getBlockStateDesc, false)
                return methodNode
            }
        },
        'xaero_pac_create_blockbreakingmovementbehaviour_tickbreaker': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.contraptions.components.actors.BlockBreakingMovementBehaviour',
                'methodName': 'tickBreaker',
                'methodDesc' : '(Lcom/simibubi/create/content/contraptions/components/structureMovement/MovementContext;)V'
            },
            'transformer' : function(methodNode){
                transformCreateBreakerMovementBehaviour(methodNode)
                return methodNode
            }
        },
        'xaero_pac_create_harvestermovementbehaviour_tickbreaker': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.contraptions.components.actors.HarvesterMovementBehaviour',
                'methodName': 'visitNewPosition',
                'methodDesc' : '(Lcom/simibubi/create/content/contraptions/components/structureMovement/MovementContext;Lnet/minecraft/core/BlockPos;)V'
            },
            'transformer' : function(methodNode){
                transformCreateBreakerMovementBehaviour(methodNode)//same exact transformation works here too
                return methodNode
            }
        },
        'xaero_pac_create_symmetrywanditem_apply': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.curiosities.symmetry.SymmetryWandItem',
                'methodName': 'apply',
                'methodDesc' : '(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V'
            },
            'transformer' : function(methodNode){
                transformCreateSymmetryWandApply(methodNode)
                return methodNode
            }
        },
        'xaero_pac_create_symmetrywanditem_remove': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.curiosities.symmetry.SymmetryWandItem',
                'methodName': 'remove',
                'methodDesc' : '(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;)V'
            },
            'transformer' : function(methodNode){
                transformCreateSymmetryWandApply(methodNode)//same exact transformer works here too
                return methodNode
            }
        },
        'xaero_pac_create_schematicannontileentity_tickprinter': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.schematics.block.SchematicannonTileEntity',
                'methodName': 'tickPrinter',
                'methodDesc' : '()V'
            },
            'transformer' : function(methodNode){
                var invokeTargetClass = 'com/simibubi/create/content/schematics/SchematicPrinter'
                var invokeTargetName = 'shouldPlaceCurrent'
                var invokeTargetNameObf = invokeTargetName
                var invokeTargetDesc = '(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/content/schematics/SchematicPrinter$PlacementPredicate;)Z'

                var insnToInsertGetter = function() {
                    var insnToInsert = new InsnList()
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                    insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/schematics/block/SchematicannonTileEntity', 'printer', 'Lcom/simibubi/create/content/schematics/SchematicPrinter;'))
                    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, 'com/simibubi/create/content/schematics/SchematicPrinter', 'getCurrentTarget', '()Lnet/minecraft/core/BlockPos;'))
                    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'canCreateCannonPlaceBlock', '(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/core/BlockPos;)Z'))
                    insnToInsert.add(new InsnNode(Opcodes.IAND))
                    return insnToInsert
                }
                insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
                return methodNode
            }
        },
        'xaero_pac_create_contraptioncollider_collideentities': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider',
                'methodName': 'collideEntities',
                'methodDesc' : '(Lcom/simibubi/create/content/contraptions/components/structureMovement/AbstractContraptionEntity;)V'
            },
            'transformer' : function(methodNode){
                transformCreateCollideEntities(methodNode)
                return methodNode
            }
        },
        'xaero_pac_create_contraptioncollider_collideblocks': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.contraptions.components.structureMovement.ContraptionCollider',
                'methodName': 'collideBlocks',
                'methodDesc' : '(Lcom/simibubi/create/content/contraptions/components/structureMovement/AbstractContraptionEntity;)Z'
            },
            'transformer' : function(methodNode){
                transformCreateCollideEntities(methodNode)//same exact transformer works here too
                return methodNode
            }
        },
        'xaero_pac_create_armtileentity_searchforitem': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.logistics.block.mechanicalArm.ArmTileEntity',
                'methodName': 'searchForItem',
                'methodDesc' : '()V'
            },
            'transformer' : function(methodNode){
                transformCreateMechArmSearch(methodNode, 'inputs')
                return methodNode
            }
        },
        'xaero_pac_create_armtileentity_searchfordestination': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.logistics.block.mechanicalArm.ArmTileEntity',
                'methodName': 'searchForDestination',
                'methodDesc' : '()V'
            },
            'transformer' : function(methodNode){
                transformCreateMechArmSearch(methodNode, 'outputs')
                return methodNode
            }
        },
		'xaero_pac_create_arminteractionpoint': {
			'target' : {
				'type' : 'CLASS',
				'name' : 'com.simibubi.create.content.logistics.block.mechanicalArm.ArmInteractionPoint'
			},
			'transformer' : function(classNode){
				var fields = classNode.fields
				classNode.interfaces.add("xaero/pac/common/server/core/accessor/ICreateArmInteractionPoint")
				addCustomGetter(classNode, 'pos', 'Lnet/minecraft/core/BlockPos;', 'xaero_OPAC_getPos')
				return classNode
			}
		},
        'xaero_pac_create_tilentityconfigurationpacket_applySettings': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.foundation.networking.TileEntityConfigurationPacket',
                'methodName': 'applySettings',
                'methodDesc' : '(Lnet/minecraft/server/level/ServerPlayer;Lcom/simibubi/create/foundation/tileEntity/SyncedTileEntity;)V'
            },
            'transformer' : function(methodNode){
                var MY_LABEL = new LabelNode(new Label())
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'isCreateTileEntityPacketAllowed', '(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/server/level/ServerPlayer;)Z'))
                insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
                insnToInsert.add(new InsnNode(Opcodes.RETURN))
                insnToInsert.add(MY_LABEL)
                methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_create_deployermovementbehaviour_activate': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.contraptions.components.deployer.DeployerMovementBehaviour',
                'methodName': 'activate',
                'methodDesc' : '(Lcom/simibubi/create/content/contraptions/components/structureMovement/MovementContext;Lnet/minecraft/core/BlockPos;Lcom/simibubi/create/content/contraptions/components/deployer/DeployerFakePlayer;Lcom/simibubi/create/content/contraptions/components/deployer/DeployerTileEntity$Mode;)V'
            },
            'transformer' : function(methodNode){
                var MY_LABEL = new LabelNode(new Label())
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))//movement context
                insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/components/structureMovement/MovementContext', 'world', 'Lnet/minecraft/world/level/Level;'))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/components/structureMovement/MovementContext', 'contraption', 'Lcom/simibubi/create/content/contraptions/components/structureMovement/Contraption;'))
                insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/components/structureMovement/Contraption', 'anchor', 'Lnet/minecraft/core/BlockPos;'))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'isCreateDeployerBlockInteractionAllowed', '(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Z'))
                insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
                insnToInsert.add(new InsnNode(Opcodes.RETURN))
                insnToInsert.add(MY_LABEL)
                methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_create_deployertilentity_activate': {
            'target' : {
                'type': 'METHOD',
                'class': 'com.simibubi.create.content.contraptions.components.deployer.DeployerTileEntity',
                'methodName': 'activate',
                'methodDesc' : '()V'
            },
            'transformer' : function(methodNode){
                var MY_LABEL = new LabelNode(new Label())
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))//movement context
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'isCreateTileDeployerBlockInteractionAllowed', '(Lnet/minecraft/world/level/block/entity/BlockEntity;)Z'))
                insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
                insnToInsert.add(new InsnNode(Opcodes.RETURN))
                insnToInsert.add(MY_LABEL)
                methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_servergamepacketlistenerimpl_handleinteract': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.server.network.ServerGamePacketListenerImpl',
                'methodName': 'm_6946_',
                'methodDesc' : '(Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;)V'
            },
            'transformer' : function(methodNode){
                var invokeTargetClass = 'net/minecraft/network/protocol/game/ServerboundInteractPacket'
                var invokeTargetName = 'dispatch'
                var invokeTargetNameObf = 'm_179617_'
                var invokeTargetDesc = '(Lnet/minecraft/network/protocol/game/ServerboundInteractPacket$Handler;)V'

                var insnToInsertGetter = function() {
                    var MY_LABEL = new LabelNode(new Label())
                    var insnToInsert = new InsnList()
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'canInteract', '(Lnet/minecraft/server/network/ServerGamePacketListenerImpl;Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;)Z'))
                    insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
                    insnToInsert.add(new InsnNode(Opcodes.RETURN))
                    insnToInsert.add(MY_LABEL)
                    return insnToInsert
                }
                insertOnInvoke2(methodNode, insnToInsertGetter, true/*before*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
                return methodNode
            }
        },
        'xaero_pac_entity_isinvulnerable': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.Entity',
                'methodName': 'm_6673_',
                'methodDesc' : '(Lnet/minecraft/world/damagesource/DamageSource;)Z'
            },
            'transformer' : function(methodNode){
                var insnToInsertGetter = function() {
                    var insnToInsert = new InsnList()
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'replaceEntityIsInvulnerable', '(ZLnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/entity/Entity;)Z'))
                    return insnToInsert
                }
                insertBeforeReturn2(methodNode, insnToInsertGetter)
                return methodNode
            }
        },
        'xaero_pac_level_destroyblock': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.Level',
                'methodName': 'm_7740_',
                'methodDesc' : '(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z'
            },
            'transformer' : function(methodNode){
                var MY_LABEL = new LabelNode(new Label())
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 3))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'canDestroyBlock', '(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)Z'))
                insnToInsert.add(new InsnNode(Opcodes.DUP))
                insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
                insnToInsert.add(new InsnNode(Opcodes.IRETURN))
                insnToInsert.add(MY_LABEL)
                insnToInsert.add(new InsnNode(Opcodes.POP))
                methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_buttonblock_checkpressed': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.ButtonBlock',
                'methodName': 'm_51120_',
                'methodDesc' : '(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V'
            },
            'transformer' : function(methodNode){
                return transformForEntitiesPushBlock(methodNode, true, false)
            }
        },
        'xaero_pac_pressureplateblock_getsignalstrength': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.PressurePlateBlock',
                'methodName': 'm_6693_',
                'methodDesc' : '(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I'
            },
            'transformer' : function(methodNode){
                return transformForEntitiesPushBlock(methodNode, true, true)
            }
        },
        'xaero_pac_weightedpressureplateblock_getsignalstrength': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.WeightedPressurePlateBlock',
                'methodName': 'm_6693_',
                'methodDesc' : '(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I'
            },
            'transformer' : function(methodNode){
                return transformForEntitiesPushBlock(methodNode, true, false)
            }
        },
        'xaero_pac_targetblock_onprojectilehit': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.TargetBlock',
                'methodName': 'm_5581_',
                'methodDesc' : '(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/entity/projectile/Projectile;)V'
            },
            'transformer' : function(methodNode){
                var MY_LABEL = new LabelNode(new Label())
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 4))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'onEntityPushBlock', '(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/entity/Entity;)Z'))
                insnToInsert.add(new JumpInsnNode(Opcodes.IFEQ, MY_LABEL))
                insnToInsert.add(new InsnNode(Opcodes.RETURN))
                insnToInsert.add(MY_LABEL)
                methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
                return methodNode
            }
        },
        'xaero_pac_frostwalkerenchantment_onentitymove': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.item.enchantment.FrostWalkerEnchantment',
                'methodName': 'm_45018_',
                'methodDesc' : '(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;I)V'
            },
            'transformer' : function(methodNode){
                var insnToInsert = new InsnList()
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'preFrostWalkHandle', '(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V'))
                methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)

                var invokeTargetClass = 'net/minecraft/world/level/Level'
                var invokeTargetName = 'getBlockState'
                var invokeTargetNameObf = 'm_8055_'
                var invokeTargetDesc = '(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;'
                var insnToInsertGetter = function() {
                    var insnToInsert = new InsnList()
                    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'preBlockStateFetchOnFrostwalk', '(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;'))
                    return insnToInsert
                }
                insertOnInvoke2(methodNode, insnToInsertGetter, true/*before*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)

                insnToInsertGetter = function() {
                    var insnToInsert = new InsnList()
                    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'postFrostWalkHandle', '()V'))
                    return insnToInsert
                }
                insertBeforeReturn2(methodNode, insnToInsertGetter)
                return methodNode
            }
        },
        'xaero_pac_entity_handlenetherportal': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.Entity',
                'methodName': 'm_20157_',
                'methodDesc' : '()V'
            },
            'transformer' : function(methodNode){
                var invokeTargetClass = 'net/minecraft/world/entity/Entity'
                var invokeTargetName = 'changeDimension'
                var invokeTargetNameObf = 'm_5489_'
                var invokeTargetDesc = '(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;'
                var insnToInsertGetter = function() {
                    var MY_LABEL = new LabelNode(new Label())
                    var insnToInsert = new InsnList()
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
                    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'onHandleNetherPortal', '(Lnet/minecraft/world/entity/Entity;)Z'))
                    insnToInsert.add(new JumpInsnNode(Opcodes.IFEQ, MY_LABEL))
                    insnToInsert.add(new InsnNode(Opcodes.RETURN))
                    insnToInsert.add(MY_LABEL)
                    return insnToInsert
                }
                insertOnInvoke2(methodNode, insnToInsertGetter, true/*before*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
                return methodNode
            }
        }
	}
}