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

function initializeCoreMod() {
	return {
		'xaero_pac_experienceorb_follownearbyplayer': {
			'target' : {
				'type': 'METHOD',
				'class': 'net.minecraft.world.entity.ExperienceOrb',
				'methodName': 'followNearbyPlayer',
				'methodDesc' : '()V'
			},
			'transformer' : function(methodNode){
				var invokeTargetClass = 'net/minecraft/world/level/Level'
				var invokeTargetName = 'getNearestPlayer'
				var invokeTargetNameObf = 'm_45930_'
				var invokeTargetDesc = '(Lnet/minecraft/world/entity/Entity;D)Lnet/minecraft/world/entity/player/Player;'
				var insnToInsertGetter = function() {
					var insnToInsert = new InsnList()
					insnToInsert.add(new VarInsnNode(Opcodes.ALOAD, 0))
					insnToInsert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'xaero/pac/common/server/core/ServerCore', 'onExperiencePickup', '(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/ExperienceOrb;)Lnet/minecraft/world/entity/player/Player;'))
					return insnToInsert
				}
				insertOnInvoke2(methodNode, insnToInsertGetter, false/*after*/, invokeTargetClass, invokeTargetName, invokeTargetNameObf, invokeTargetDesc, false)
				return methodNode
			}
		}
	}
}