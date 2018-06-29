package com.diamond.deob;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Iterator;

public class RemoveUnusedFields extends DeobTransformer {
    RemoveUnusedFields(Deobfuscator deobfuscator) {
        super(deobfuscator);
    }

    // If the class is a superclass, verify that none of its
    // inheritors depend on the field in question

    // Also clears inherited fields
    private boolean fieldInherited(String superName, FieldNode field) {
        // If the class is not a superclass, then it's impossible for anything to inherit the field
        if (!deobfuscator.supers.containsKey(superName)) {
            return false;
        }

        ClassNode superClass = deobfuscator.classes.get(superName);
        System.out.println("Checking super: " + superClass.name + " for " + field.name + ":" + field.desc);

        // Rename stuff. all the nots are confusing
        for (ClassNode inheritor : deobfuscator.supers.get(superName)) {
            System.out.println("\tChecking inheritor: " + inheritor.name);
            // If the field does not have a direct assignment, and the same field name and descriptor is found
            // in the superclass, then the field has a dependency on the superclass.
            // Note that the JVM doesn't add bytecode to initialize variables to zero
            // If there is a direct assignment, then the field is overridden in the inheritor class.
            // Superclass and inheritor share a field descriptor

            // Inheritor must have no assignment and at some point accesses the field (may be redundant)
            // or some eventual inheritor inherits

            // Check for field assignment!
            // Do I need to do a mark if this fails?

            String desc = Util.getFieldDescriptor(inheritor, field);
            // So if the class does not define the field, but it accesses the field, then it is inherited.
            // We must also remove this access from the field-removal list.
            // If the class does not define the field, and does not access the field,
            if ((!Util.classDefinesField(inheritor, desc) && Util.classAccessesField(inheritor, desc)) ||
                    fieldInherited(inheritor.name, field)) {
                // Need to remove inheritor desc from marked?
                System.out.println("\tField is inherited.. Skipping");
                deobfuscator.markedMethods.remove(desc);
                return true;
            } else if (!Util.classAccessesField(inheritor, desc)) {
                // If we have an inheritor that never accesses the field, then references need removed.
                deobfuscator.markedMethods.put(desc, 0);
            }
        }
        return false;
    }

    @Override
    void run(ClassNode clazz) {
        System.out.println("Analyzing class: " + clazz.name + ((deobfuscator.isSuper(clazz.name)) ? " (super)" : ""));
        Iterator<FieldNode> fields = clazz.fields.iterator();
        while (fields.hasNext()) {
            FieldNode field = fields.next();
            String desc = Util.getFieldDescriptor(clazz, field);

            if (!deobfuscator.hasMarked(desc)) {
                System.out.println("Removing field: " + desc);
                fields.remove();
                // Add descriptor for later removal
                deobfuscator.markedMethods.put(desc, 0);
            } else {
                deobfuscator.markedMethods.remove(desc);
            }

            // If dealing with a superclass, and the field is unused in the super, then check if the field
            // is inherited. If it is, don't remove it.
            /*
            if (deobfuscator.isSuper(clazz.name)) {
                if (fieldInherited(clazz.name, field) && !deobfuscator.hasMarked(desc)) {
                    deobfuscator.markedMethods.remove(desc);
                    continue;
                }
            }

            // Otherwise, the class has no inheritors, so removing fields is safe.
            if (!deobfuscator.hasMarked(desc)) {
                System.out.println("Removing field: " + desc);
                fields.remove();
                // Add descriptor for later removal
                deobfuscator.markedMethods.put(desc, 0);
            } else {
                // Once this transformer is finished, the only fields left marked are those that were removed.
                // This makes it easier to remove any field assignments in the bytecode.
                deobfuscator.markedMethods.remove(desc);
            }
            */

            /*
            if (!deobfuscator.markedMethods.containsKey(desc) && !fieldInherited(clazz.name, field)) {
                System.out.println("Removing field: " + desc);
                fields.remove();
                // The problem is that inherited fields are not iterated over, and therefore never removed
                // from the list. How to make sure that they're removed?

                // Mark the removed field for later processing
                //System.out.println("Marking: " + desc);
                deobfuscator.markedMethods.put(desc, 0);
            } else {
                // Once this transformer is finished, the only fields left marked are those that were removed.
                // This makes it easier to remove any field assignments in the bytecode
                //System.out.println("Unmarking: " + desc);
                deobfuscator.markedMethods.remove(desc);
            }
            */
        }
    }
}
