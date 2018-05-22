package com.diamond.deob;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

public class RemoveUnusedFields extends DeobTransformer {
    RemoveUnusedFields(Deobfuscator deobfuscator) {
        super(deobfuscator);
    }

    @Override
    void run(ClassNode clazz) {
        Iterator<FieldNode> fields = clazz.fields.iterator();
        while (fields.hasNext()) {
            FieldNode field = fields.next();
            if (!deobfuscator.markedMethods.contains(clazz.name + "." + field.name + field.desc)) {
                System.out.println("Removing field: " + clazz.name + "." + field.name + field.desc);
                fields.remove();
            }
        }
    }
}
