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
		},
		'xaero_pac_create_deployermovementbehaviour_activate': {
			'target' : {
				'type': 'METHOD',
				'class': 'com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour',
				'methodName': 'activate',
				'methodDesc' : '(Lcom/simibubi/create/content/contraptions/behaviour/MovementContext;Lnet/minecraft/core/BlockPos;Lcom/simibubi/create/content/kinetics/deployer/DeployerFakePlayer;Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity\$Mode;)V'
			},
			'transformer' : function(methodNode){
				var MY_LABEL = new LabelNode(new Label())
				var insnToInsert = new InsnList()
				insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))//movement context
				insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/behaviour/MovementContext', 'world', 'Lnet/minecraft/world/level/Level;'))
				insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 1))
				insnToInsert.add(new FieldInsnNode(Opcodes.GETFIELD, 'com/simibubi/create/content/contraptions/behaviour/MovementContext', 'contraption', 'Lcom/simibubi/create/content/contraptions/Contraption;'))
				insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 2))
				insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'isCreateDeployerBlockInteractionAllowed', '(Lnet/minecraft/world/level/Level;Lxaero/pac/common/server/core/accessor/ICreateContraption;Lnet/minecraft/core/BlockPos;)Z'))
				insnToInsert.add(new JumpInsnNode(Opcodes.IFNE, MY_LABEL))
				insnToInsert.add(new InsnNode(Opcodes.RETURN))
				insnToInsert.add(MY_LABEL)
				methodNode.instructions.insert(methodNode.instructions.get(0), insnToInsert)
				return methodNode
			}
		}
	}
}