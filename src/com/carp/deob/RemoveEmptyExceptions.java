package com.carp.deob;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.Iterator;

public class RemoveEmptyExceptions extends DeobTransformer {
    RemoveEmptyExceptions(Main.Deobfuscator deobfuscator) {
        super(deobfuscator);
    }

    @Override
    void run(ClassNode clazz) {
        for (MethodNode method : clazz.methods) {
            Iterator<TryCatchBlockNode> blocks = method.tryCatchBlocks.iterator();
            while (blocks.hasNext()) {
                TryCatchBlockNode block = blocks.next();
                if (block.start.getNext() == block.end.getPrevious()) {
                    System.out.println("Removing empty try catch from: " + method.name + method.desc);
                    blocks.remove();
                }
            }
        }
    }
}
