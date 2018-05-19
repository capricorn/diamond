package com.diamond.deob;

import com.diamond.JarSearch;
import com.diamond.Loader;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Deobfuscator {
    private HashMap<String, ClassNode> classes = new HashMap<>();
    HashSet<String> blacklistedClasses = new HashSet<>();
    HashSet<String> markedMethods = new HashSet<>();
    Loader loader;

    Deobfuscator(String jarFile) {
        try {
            loader = new Loader(jarFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create loader for deobfuscator");
        }

        for (JavaClass jc : new JarSearch(jarFile).getClasses()) {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(jc.getBytes());

            cr.accept(cn, 0);
            classes.put(jc.getClassName(), cn);
        }
    }

    // Could have method return this, reduce some redundancy
    private void transform(DeobTransformer operation) {
        for (ClassNode clazz : classes.values()) {
            operation.run(clazz);
            // Add a call that handles clearing state
            // operation.updateState()?
        }
    }

    private void transform(DeobTransformer operation, String clazz) {
        operation.run(classes.get(clazz));
    }

    public void write() {
        transform(new WriteClass(this));
    }

    public Deobfuscator markUsedFields() {
        for (ClassNode clazz : classes.values()) {
            for (MethodNode methods : clazz.methods) {
                for (AbstractInsnNode insn : methods.instructions.toArray()) {
                    if (insn instanceof FieldInsnNode) {
                        FieldInsnNode field = ((FieldInsnNode) insn);
                        markedMethods.add(field.owner + "." + field.name + field.desc);
                    }
                }
            }
        }
        return this;
    }

    public Deobfuscator removeUnusedFields() {
        transform(new RemoveUnusedFields(this));
        return this;
    }

    // How to chain for deleting unused classes ?
    public Deobfuscator removeUnusedClasses() {
        markedMethods.addAll(classes.keySet());
        transform(new RemoveUnusedClasses(this), "client");
        markedMethods.clear();
        return this;
    }

    public Deobfuscator removeCallsToMarkedMethods() {
        transform(new RemoveDeadMethodCalls(this));
        markedMethods.clear();  // May not necessarily clear; may create new marked methods
        return this;
    }

    public Deobfuscator removeEmptyExceptions() {
        transform(new RemoveEmptyExceptions(this));
        return this;
    }

    /**
     * Removes every method found in bytecode such that the first instruction is RETURN.
     * After removing the method from the class, the method descriptor is added to markedMethods.
     * @return
     */
    public Deobfuscator removeEmptyMethods() {
        transform(new RemoveEmptyMethods(this));
        return this;
    }
}
