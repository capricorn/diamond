package com.diamond.deob;

import com.diamond.JarSearch;
import com.diamond.Loader;
import org.apache.bcel.classfile.JavaClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Deobfuscator {
    HashMap<String, ClassNode> classes = new HashMap<>();
    HashSet<String> blacklistedClasses = new HashSet<>();
    HashMap<String, Integer> markedMethods = new HashMap<>();
    HashMap<String, LinkedList<ClassNode>> supers = new HashMap<>();
    HashMap<String, LinkedList<ClassNode>> interfaces = new HashMap<>();
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

            // interfaces implemented by cn (including those from outside the gamepack)
            for (String itf : cn.interfaces) {
                LinkedList<ClassNode> implementors = interfaces.get(itf);
                if (implementors == null) {
                    implementors = new LinkedList<>();
                    interfaces.put(itf, implementors);
                }
                implementors.add(cn);
            }

            // supers implemented by cn (including those from outside the gamepack, except Object)
            if (!cn.superName.equals("java/lang/Object")) {
                LinkedList<ClassNode> inheritors = supers.get(cn.superName);
                if (inheritors == null) {
                    inheritors = new LinkedList<>();
                    supers.put(cn.superName, inheritors);
                }
                inheritors.add(cn);
            }

            classes.put(jc.getClassName(), cn);
        }
    }

    private void transform(DeobTransformer operation) {
        for (ClassNode clazz : classes.values()) {
            operation.run(clazz);
        }
    }

    private void transform(DeobTransformer operation, String clazz) {
        operation.run(classes.get(clazz));
    }

    public void write() {
        transform(new WriteClass(this));
    }

    public Deobfuscator markedUsedClasses() {
        LinkedList<String> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();

        queue.add("client");
        visited.add("client");
        addToCounter("client");

        while (!queue.isEmpty()) {
            String name = queue.removeFirst();

            // Obtain neighbors
            HashSet<String> refs = Util.getClassRefs(name);
            // Count every occurrence == linkage count
            for (String ref: refs) {
                addToCounter(ref);
            }
            // Remove neighbors we've already visited
            refs.removeAll(visited);
            // Add new neighbors to queue for processing
            queue.addAll(refs);
            // Mark new references as visited
            visited.addAll(refs);
        }
        return this;
    }

    public boolean isSuper(String className) {
        return supers.containsKey(className);
    }

    public boolean hasMarked(String item) {
        return markedMethods.containsKey(item);
    }

    public void mark(String item, int count) {
        markedMethods.put(item, count);
    }

    public void mark(String item) {
        markedMethods.put(item, 0);
    }

    // A field is used if it is accessed, i.e. getstatic or getfield
    public Deobfuscator markUsedFields() {
        for (ClassNode clazz : classes.values()) {
            for (MethodNode method : clazz.methods) {
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (insn instanceof FieldInsnNode) {
                        FieldInsnNode field = (FieldInsnNode) insn;
                        // If a field is never accessed, then it's useless
                        if ((field.getOpcode() == Opcodes.GETSTATIC) || field.getOpcode() == Opcodes.GETFIELD) {
                            System.out.println("Field access: " + field.owner + "." + field.name + ":" + field.desc);
                            String defDesc = getDefiningClassName(field.owner, field.name + ":" + field.desc) + "." + field.name + ":" + field.desc;
                            mark(defDesc);
                        }
                    }
                }
            }
        }
        return this;
    }

    public Deobfuscator removeUnusedFields() {
        transform(new RemoveUnusedFields(this));
        transform(new RemoveDeadFieldAssignments(this));
        markedMethods.clear();
        return this;
    }

    protected void addToCounter(String item) {
        if (!markedMethods.containsKey(item)) {
            markedMethods.put(item, 1);
            return;
        }
        markedMethods.put(item, markedMethods.get(item)+1);
    }

    protected int removeFromCounter(String item) {
        if (!markedMethods.containsKey(item)) {
            return -1;
        }

        int count = markedMethods.get(item) - 1;
        if (count == 0) {
            markedMethods.remove(item);
            return 0;
        }

        markedMethods.put(item, count);
        return count;
    }

    // How to chain for deleting unused classes ?
    public Deobfuscator removeUnusedClasses() {
        //markedMethods.addAll(classes.keySet());
        //transform(new RemoveUnusedClasses(this), "client");
        transform(new RemoveUnusedClasses(this));
        markedMethods.clear();
        return this;
    }

    /*
    public Deobfuscator removeCallsToMarkedMethods() {
        transform(new RemoveDeadMethodCalls(this));
        markedMethods.clear();  // May not necessarily clear; may create new marked methods
        return this;
    }
    */

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

    private String getDefiningClassName(String className, String fieldNameType) {
        if (className.startsWith("java")) {
            return className;
        }

        ClassNode clazz = classes.get(className);
        if (Util.classDefinesField(clazz, clazz.name + "." + fieldNameType)) {
            return clazz.name;
        }

        return getDefiningClassName(clazz.superName, fieldNameType);
    }
}
