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

    private void removeUnusedClass(String name) {
        deobfuscator.blacklistedClasses.add(name);
        HashSet<String> refs = Util.getClassRefs(name);
        for (String ref : refs) {
            if (deobfuscator.removeFromCounter(ref) == 0) {
                System.out.println("\tFound newly unused class: " + ref);
                removeUnusedClass(ref);
            }
        }
    }

    @Override
    void run(ClassNode clazz) {
        if (!deobfuscator.markedMethods.containsKey(clazz.name)) {
            System.out.println("Found unused class: " + clazz.name);

            // Modify BFS to correctly track count
            removeUnusedClass(clazz.name);
        }
    }
}
