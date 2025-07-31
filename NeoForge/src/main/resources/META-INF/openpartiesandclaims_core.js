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

var addFreshEntityName = 'addFreshEntity'
var addFreshEntityNameObf = 'm_7967_'
var addFreshEntityDesc = '(Lnet/minecraft/world/entity/Entity;)Z'

function addCustomGetter2(classNode, fieldName, fieldDesc, methodName, methodDesc){
	var methods = classNode.methods
	var getterNode = new MethodNode(Opcodes.ACC_PUBLIC, methodName, "()" + methodDesc, null, null)
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

function addCustomGetter(classNode, fieldName, fieldDesc, methodName){
	addCustomGetter2(classNode, fieldName, fieldDesc, methodName, fieldDesc)
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

function getCreateModBlockBreakContraptionHandlerInsn(){
    return new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'replaceBlockFetchOnCreateModBreak', '(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lxaero/pac/common/server/core/accessor/ICreateContraption;)Lnet/minecraft/world/level/block/state/BlockState;')
}

function transformCreateBreakerMovementBehaviour(methodNode){
    insertCreateModBlockPosArgumentCapture(methodNode, levelClass, getBlockStateName, getBlockStateNameObf, getBlockStateDesc)

    var insnToInsertGetter = function() {
        var insnToInsert = new InsnList()
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))//movement context
        insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/behaviour/MovementContext', 'world', 'Lnet/minecraft/world/level/Level;'))
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
        insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/behaviour/MovementContext', 'contraption', 'Lcom/simibubi/create/content/contraptions/Contraption;'))
        insnToInsert.add(getCreateModBlockBreakContraptionHandlerInsn())
        return insnToInsert
    }
    insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, levelClass, getBlockStateName, getBlockStateNameObf, getBlockStateDesc, false)
    return methodNode
}

function transformCreateSymmetryWandApply(methodNode){
    var invokeTargetClass = 'com/simibubi/create/content/equipment/symmetryWand/mirror/SymmetryMirror'
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
        insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/AbstractContraptionEntity', 'contraption', 'Lcom/simibubi/create/content/contraptions/Contraption;'))
        insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'onCreateCollideEntities', '(Ljava/util/List;Lnet/minecraft/world/entity/Entity;Lxaero/pac/common/server/core/accessor/ICreateContraption;)V'))
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
    insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/kinetics/mechanicalArm/ArmBlockEntity', listFieldName, 'Ljava/util/List;'))
    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'isCreateMechanicalArmValid', '(Lnet/minecraft/world/level/block/entity/BlockEntity;Ljava/util/List;)Z'))
    insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
    insnToInsert.add(new InsnNode(Opcodes.RETURN))
    insnToInsert.add(MY_LABEL)
    methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
}

function transformCreateTileEntityPacket(methodNode, packetClass, posField){
    var MY_LABEL = new LabelNode(new Label())
    var insnToInsert = new InsnList()
    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
    insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, packetClass, posField, "Lnet/minecraft/core/BlockPos;"))
    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCoreNeoForge', 'isCreateTileEntityPacketAllowed', '(Lnet/minecraft/core/BlockPos;Lnet/minecraft/server/level/ServerPlayer;)Z'))
    insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
    insnToInsert.add(new InsnNode(Opcodes.RETURN))
    insnToInsert.add(MY_LABEL)
    methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
    return methodNode
}

function transformForEntitiesPushBlock(methodNode, includeClassFiltered, includeNonClassFiltered, blockPosArgIndex){
    var invokeTargetClass = 'net/minecraft/world/level/Level'
    var insnToInsertGetter = function() {
        var insnToInsert = new InsnList()
        insnToInsert.add(new InsnNode(Opcodes.DUP))
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, blockPosArgIndex))
        insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'onEntitiesPushBlock', '(Ljava/util/List;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;)V'))
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

function transformPrePostLivingDeath(methodNode, preMethodName, postMethodName){
    var insnToInsert = new InsnList()
    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', preMethodName, "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)

    var insnToInsertGetter = function() {
        var insnToInsert = new InsnList()
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
        insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', postMethodName, "(Lnet/minecraft/world/entity/LivingEntity;)V"))
        return insnToInsert
    }
    insertBeforeReturn2(methodNode, insnToInsertGetter)
    return methodNode
}

function transformPrePostResourcesDrop(methodNode, entityParIndex){
    var insnToInsert = new InsnList()
    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, entityParIndex))
    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'preResourcesDrop', '(Lnet/minecraft/world/entity/Entity;)V'))
    methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)

    var insnToInsertGetter = function() {
         var insnToInsert = new InsnList()
         insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, entityParIndex))
         insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'postResourcesDrop', '(Lnet/minecraft/world/entity/Entity;)V'))
         return insnToInsert
    }
    insertBeforeReturn2(methodNode, insnToInsertGetter)
    return methodNode
}

function transformProjectileHitCapture(methodNode, projectileClass, preMethodName, postMethodName){
    var invokeTargetClass = projectileClass
    var invokeTargetName = 'onHit'
    var invokeTargetNameObf = 'm_6532_'
    var invokeTargetDesc = '(Lnet/minecraft/world/phys/HitResult;)V'
    var insnToInsertGetter = function() {
        var insnToInsert = new InsnList()
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
        insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', preMethodName, '(Lnet/minecraft/world/entity/projectile/Projectile;)V'))
        return insnToInsert
    }
    insertOnInvoke2(methodNode, insnToInsertGetter, true/*before*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
    insnToInsertGetter = function() {
        var insnToInsert = new InsnList()
        insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
        insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', postMethodName, '(Lnet/minecraft/world/entity/projectile/Projectile;)V'))
        return insnToInsert
    }
    insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
    return methodNode
}

function initializeCoreMod() {
	return {
        'xaero_pac_entitygetter_getentitycollisions': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.EntityGetter',
                'methodName': 'getEntityCollisions',
                'methodDesc' : '(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;'
            },
            'transformer' : function(methodNode){
                var invokeTargetClass = 'net/minecraft/world/level/EntityGetter'
                var invokeTargetName = 'getEntities'
                var invokeTargetNameObf = 'm_6249_'
                var invokeTargetDesc = '(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;'
                var insnToInsertGetter = function() {
                    var insnToInsert = new InsnList()
                    insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
                    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'onEntitiesPushEntity', '(Ljava/util/List;Lnet/minecraft/world/entity/Entity;)Ljava/util/List;'))
                    return insnToInsert
                }
                insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
                return methodNode
            }
        },
        'xaero_pac_buttonblock_checkpressed': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.ButtonBlock',
                'methodName': 'checkPressed',
                'methodDesc' : '(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V'
            },
            'transformer' : function(methodNode){
                return transformForEntitiesPushBlock(methodNode, true, false, 3)
            }
        },
        'xaero_pac_basepressureplateblock_getentitycount': {
            'target' : {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.BasePressurePlateBlock',
                'methodName': 'getEntityCount',
                'methodDesc' : '(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/AABB;Ljava/lang/Class;)I'
            },
            'transformer' : function(methodNode){
                var invokeTargetClass = 'java/util/List'
                var invokeTargetName = 'size'
                var invokeTargetNameObf = invokeTargetName
                var invokeTargetDesc = '()I'

                var insnToInsertGetter = function() {
                    var insnToInsert = new InsnList()
                    insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'onPressurePlateEntityCount', '(Ljava/util/List;)Ljava/util/List;'))
                    return insnToInsert
                }
                insertOnInvoke2(methodNode, insnToInsertGetter, true/*before*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
                return methodNode
            }
        }
	}
}