package com.diamond.deob;

import org.objectweb.asm.tree.ClassNode;

abstract class DeobTransformer {
    Deobfuscator deobfuscator;
    int statistics;

    DeobTransformer(Deobfuscator deobfuscator) {
        this.deobfuscator = deobfuscator;
    }

    abstract void run(ClassNode clazz);
}
