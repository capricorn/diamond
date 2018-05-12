package com.carp.deob;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;

public class RemoveEmptyMethods extends DeobTransformer {
    RemoveEmptyMethods(Deobfuscator deobfuscator) {
        super(deobfuscator);
    }

    @Override
    public void run(ClassNode clazz) {
        Iterator<MethodNode> methods = clazz.methods.iterator();
        while (methods.hasNext()) {
            MethodNode method = methods.next();
            if (method.name.length() > 2) {
                // Assume this is an interface method
                continue;
            }

            if ((method.access & ACC_ABSTRACT) != 0) {
                continue;
            }
            // Abstract methods have to be implemented, even if that
            // just means void


            for (AbstractInsnNode ins : method.instructions.toArray()) {
                int op = ins.getOpcode();
                if (op == -1) {
                    continue;
                }
                if (op == Opcodes.RETURN) {
                    System.out.println("Removing entire method: " + clazz.name + "." + method.name + method.desc);
                    deobfuscator.markedMethods.add(clazz.name + "." + method.name + method.desc);
                    methods.remove();
                }
                break;
            }
        }
    }
}
