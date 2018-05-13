package com.diamond.deob;

import com.diamond.JarSearch;
import com.diamond.Loader;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

// How should this work for a jar though?
// Perhaps all operations should be performed on every class
// of the given jar, but internal state maintained for things changed,
// such as a list of removed methods, etc
// utilize java streams when traversing, make each special deob part
// have a functional interface for calling?
// have operations that mark objects,
// and operations that operate on marked objects
// perhaps utilize a classloader and just pass class names
// initialize a classnode for every class?
public class Deobfuscator {
    private ArrayList<ClassNode> classes = new ArrayList<>();
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
            classes.add(cn);
        }
    }

    // Could have method return this, reduce some redundancy
    private void transform(DeobTransformer operation) {
        for (ClassNode clazz : classes) {
            operation.run(clazz);
            // Add a call that handles clearing state
            // operation.updateState()?
        }
    }

    public void write() {
        transform(new WriteClass(this));
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
