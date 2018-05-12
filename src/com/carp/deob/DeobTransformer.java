package com.carp.deob;

import org.objectweb.asm.tree.ClassNode;

abstract class DeobTransformer {
    Main.Deobfuscator deobfuscator;

    DeobTransformer(Main.Deobfuscator deobfuscator) {
        this.deobfuscator = deobfuscator;
    }

    abstract void run(ClassNode clazz);
}
