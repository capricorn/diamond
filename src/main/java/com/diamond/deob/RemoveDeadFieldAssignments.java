package com.diamond.deob;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class RemoveDeadFieldAssignments extends DeobTransformer {
    RemoveDeadFieldAssignments(Deobfuscator deobfuscator) {
        super(deobfuscator);
    }

    @Override
    void run(ClassNode clazz) {
        for (MethodNode method : clazz.methods) {
            ListIterator<AbstractInsnNode> insns = method.instructions.iterator();
            while (insns.hasNext()) {
                AbstractInsnNode insn = insns.next();
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    String desc = fieldInsn.owner + "." + fieldInsn.name + ":" + fieldInsn.desc;

                    if (fieldInsn.getOpcode() == Opcodes.PUTSTATIC) {
                        if (deobfuscator.markedMethods.containsKey(desc)) {
                            System.out.println("Removing broken access to static field: " + desc);
                            insns.remove();
                            if (fieldInsn.desc.equals("J") || fieldInsn.desc.equals("D")) {
                                insns.add(new InsnNode(Opcodes.POP2));
                            } else {
                                insns.add(new InsnNode(Opcodes.POP));
                            }
                        }
                    // Have to properly remove stack instructions
                    } else if (fieldInsn.getOpcode() == Opcodes.PUTFIELD) {
                        if (deobfuscator.markedMethods.containsKey(desc)) {
                            System.out.println("Removing broken access to instance field: " + desc);
                            insns.remove();
                            // Has to be done -- See difference between cat1 and cat2 types in the JVM
                            if (fieldInsn.desc.equals("J") || fieldInsn.desc.equals("D")) {
                                insns.add(new InsnNode(Opcodes.POP2));
                                insns.add(new InsnNode(Opcodes.POP));
                            } else {
                                insns.add(new InsnNode(Opcodes.POP2));
                                //insns.add(new InsnNode(Opcodes.POP));
                            }
                            //insns.add(new InsnNode(Opcodes.POP2));
                        }
                    }
                }
            }
        }
    }
}
