package com.diamond.deob;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class WriteClass extends DeobTransformer {
    private class SpecialClassWriter extends ClassWriter {
        public SpecialClassWriter(int flags) {
            super(flags);
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            try {
                Class clazz1 = deobfuscator.loader.loadClass(type1.replace('/', '.'));
                Class clazz2 = deobfuscator.loader.loadClass(type2.replace('/', '.'));
                // Could maybe handle recursively?
                while (!clazz1.equals(clazz2)) {
                    clazz1 = clazz1.getSuperclass();
                    clazz2 = clazz2.getSuperclass();
                }
                //System.out.println("Name: " + clazz1.getName());
                //return clazz1.getName();
                return clazz1.getName().replace('.', '/');
            } catch (RuntimeException | ClassNotFoundException e) {
                //return "java.lang.Object";
                return "java/lang/Object";
            }
        }
    }

    WriteClass(Deobfuscator deobfuscator) {
        super(deobfuscator);
    }

    @Override
    void run(ClassNode clazz) {
        System.out.println("Writing: " + clazz.name);
        SpecialClassWriter cw = new SpecialClassWriter(0);
        clazz.accept(cw);
    }
}
