package com.diamond.deob;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

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
            String desc = Util.getFieldDescriptor(clazz, field);

            if (!deobfuscator.hasMarked(desc)) {
                System.out.println("Removing field: " + desc);
                fields.remove();
                // Add descriptor for later removal
                deobfuscator.markedMethods.put(desc, 0);
                statistics++;
            } else {
                // Once this transformer is finished, the only fields left marked are those that were removed.
                // This makes it easier to remove any field assignments in the bytecode.
                deobfuscator.markedMethods.remove(desc);
            }
        }
    }
}
