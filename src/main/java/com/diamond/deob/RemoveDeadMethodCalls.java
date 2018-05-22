package com.diamond.deob;


import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;
import java.util.ListIterator;

import static com.diamond.deob.Util.countArguments;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;


public class RemoveDeadMethodCalls extends DeobTransformer {
    RemoveDeadMethodCalls(Deobfuscator deobfuscator) {
        super(deobfuscator);
    }

    @Override
    void run(ClassNode clazz) {
        if (deobfuscator.markedMethods.isEmpty()) {
            return;
        }
        System.out.println("Removing methods calls from class");
        Iterator<MethodNode> it = clazz.methods.iterator();
        // Shouldn't remove interface calls at all
        while (it.hasNext()) {
            MethodNode method = it.next();
            ListIterator<AbstractInsnNode> itInsn = method.instructions.iterator();
            while (itInsn.hasNext()) {
                AbstractInsnNode insn = itInsn.next();
                MethodInsnNode methodInsn = null;
                int argCount = -1;

                if (insn instanceof MethodInsnNode) {
                    methodInsn = (MethodInsnNode) insn;
                } else {
                    continue;
                }

                if (insn.getOpcode() == INVOKEVIRTUAL || insn.getOpcode() == INVOKEINTERFACE) {
                    argCount = countArguments(methodInsn.desc) + 1;
                } else if (insn.getOpcode() == INVOKESTATIC) {
                    argCount = countArguments(methodInsn.desc);
                }

                // Should have printed twice?
                if (argCount > -1 && deobfuscator.markedMethods.contains(methodInsn.owner + "." + methodInsn.name + methodInsn.desc)) {
                    System.out.println("Removing call to method: " + methodInsn.owner + "." + methodInsn.name + methodInsn.desc +
                    " from: " + clazz.name + "." + method.name + method.desc);

                    // Remove the invoke* call
                    itInsn.remove();
                    if (itInsn.hasPrevious()) {
                        insn = itInsn.previous();  // Is there always a previous?
                    }

                    while (argCount > 0) {
                        // Add invoke to this ?
                        if (insn.getOpcode() == INVOKESTATIC) {
                            argCount += countArguments(((MethodInsnNode) insn).desc);
                        } else if (insn.getOpcode() == INVOKEVIRTUAL || insn.getOpcode() == INVOKEINTERFACE) {
                            if (insn.getOpcode() == INVOKEINTERFACE) {
                                System.out.println("Removing interface call to: " + methodInsn.owner + "." + methodInsn.name + methodInsn.desc);
                            }
                            argCount += countArguments(((MethodInsnNode) insn).desc) + 1;
                        }
                        //if (isStackPushIns(insn.getOpcode())) {
                        if (Util.OpcodeStackNetEffect.containsKey(insn.getOpcode())) {   // Excludes invoke*..
                            System.out.println("\t" + insn.getOpcode());
                            argCount -= Util.OpcodeStackNetEffect.get(insn.getOpcode());
                            itInsn.remove();
                            insn = itInsn.previous();
                            //argCount--;
                        } else {
                            insn = itInsn.previous();
                        }
                    }
                }
            }
        }
    }
}
