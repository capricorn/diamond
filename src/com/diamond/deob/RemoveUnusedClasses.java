package com.diamond.deob;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.JavaClass;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

public class RemoveUnusedClasses extends DeobTransformer {
    RemoveUnusedClasses(Deobfuscator deobfuscator) {
        super(deobfuscator);
    }

    // Assume this transform is only performed on a single class (client.class)
    @Override
    void run(ClassNode clazz) {
        deobfuscator.markedMethods.remove(clazz.name);
        LinkedList<String> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();
        visited.add(clazz.name);
        queue.add(clazz.name);

        while (!queue.isEmpty()) {
            try {
                String className = queue.removeFirst();
                // Prefer to find a way around loading this from disk again
                JavaClass jc = new ClassParser("/tmp/gp/" + className + ".class").parse();
                for (Constant con : jc.getConstantPool().getConstantPool()) {
                    if (con == null) {
                        continue;
                    }
                    if (con.getTag() == Const.CONSTANT_Class) {
                        String refClassName = (String) ((ConstantClass) con).getConstantValue(jc.getConstantPool());
                        if (!className.equals(refClassName) && !visited.contains(refClassName) &&
                                !refClassName.startsWith("java") && !refClassName.startsWith("[") &&
                                !refClassName.startsWith("netscape")) {
                            queue.add(refClassName);
                            visited.add(refClassName);
                            deobfuscator.markedMethods.remove(refClassName);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        deobfuscator.blacklistedClasses.addAll(deobfuscator.markedMethods);
    }
}
