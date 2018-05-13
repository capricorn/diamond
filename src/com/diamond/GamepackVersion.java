package com.diamond;

import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class GamepackVersion {
    public static void main(String[] args) {
        for (JavaClass jc : new JarSearch("/tmp/tmp.jar").getClasses()) {
            if (jc.getClassName().equals("client")) {
                System.out.println("Version: " + getVersion(jc));
            }
        }
    }

    private static int getVersion(JavaClass clientClass) {
        ClassNode client = new ClassNode();
        ClassReader cr = new ClassReader(clientClass.getBytes());
        cr.accept(client, 0);

        for (MethodNode method : client.methods) {
            if (method.name.equals("init")) {
                AbstractInsnNode lastOp =  method.instructions.getLast();
                System.out.println("Last instruction: " + lastOp.getOpcode());
                while (lastOp.getPrevious() != null && lastOp.getOpcode() != Opcodes.RETURN) {
                    lastOp = lastOp.getPrevious();
                    System.out.println(lastOp.getOpcode());
                }
                if (lastOp.getOpcode() == Opcodes.RETURN) {
                    // Anyway to utilize descriptor?
                    int i = 0;
                    while (i < 3) {
                        lastOp = lastOp.getPrevious();
                        if (lastOp.getOpcode() == -1) {
                            continue;
                        }
                        i++;
                    }
                    return ((IntInsnNode) lastOp).operand;
                }
            }
        }
        return -1;
    }
}
