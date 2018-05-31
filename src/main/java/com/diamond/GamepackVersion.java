package com.diamond;

import org.apache.bcel.classfile.JavaClass;
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
                System.out.println(getVersion(jc.getBytes()));
            }
        }
    }

    /**
     * Read the current gamepack version from client.class.
     *
     * In client.class, at the end of the init (NOT <init>) method, there is a method call
     * with several parameters passed. The third parameter is the gamepack version,
     * assuming the class is still completely obfuscated.
     *
     * One way to obtain this value is to grab the last instruction of the method, and
     * traverse backwards until we reach the first return opcode, which occurs just
     * after the method call we're interested in. Once that's found, it's easy to simply
     * traverse backwards past the other stack instructions (arguments to the method) until
     * reaching the instruction that pushes the version number. From here, the number is extracted
     * and returned.
     * @param clazz
     *  Bytes from client.class
     * @return
     *  Version of the gamepack
     */
    public static int getVersion(byte[] clazz) {
        ClassNode client = new ClassNode();
        ClassReader cr = new ClassReader(clazz);
        cr.accept(client, 0);

        for (MethodNode method : client.methods) {
            if (method.name.equals("init")) {
                AbstractInsnNode lastOp =  method.instructions.getLast();
                while (lastOp.getPrevious() != null && lastOp.getOpcode() != Opcodes.RETURN) {
                    lastOp = lastOp.getPrevious();
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
