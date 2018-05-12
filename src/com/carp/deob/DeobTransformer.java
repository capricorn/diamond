package com.carp.deob;

import org.objectweb.asm.tree.ClassNode;

abstract class DeobTransformer {
    Deobfuscator deobfuscator;

    DeobTransformer(Deobfuscator deobfuscator) {
        this.deobfuscator = deobfuscator;
    }

    abstract void run(ClassNode clazz);
}
