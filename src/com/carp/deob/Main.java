package com.carp.deob;

import com.carp.JarSearch;
import com.carp.Loader;
//import com.carp.Util;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.*;
import java.util.*;

// Should not import internal!
import static org.objectweb.asm.Opcodes.*;

public class Main {


    // Rename to gamepack
    private static class SpecialClassWriter extends ClassWriter {
        public SpecialClassWriter(int flags) {
            super(flags);
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            try {
                Class clazz1 = loader.loadClass(type1.replace('/', '.'));
                Class clazz2 = loader.loadClass(type2.replace('/', '.'));
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
    private static Loader loader = null;
    static {
        try {
            loader = new Loader("/tmp/tmp.jar");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //static ClassWriter specialCW = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
    // Does reusing the same classwriter cause problems?
    // My guess is that there is unreachable code in the class, that requires padding
    // for stackframes?
    // How to prevent generation of stack frames?
    /*
    static ClassWriter specialCW = new ClassWriter(0) {
        private Loader loader = null;

        {
            try {
                loader = new Loader("/tmp/tmp.jar");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            try {
                Class clazz1 = loader.loadClass(type1.replace('/', '.'));
                Class clazz2 = loader.loadClass(type2.replace('/', '.'));
                while (!clazz1.equals(clazz2)) {
                    clazz1 = clazz1.getSuperclass();
                    clazz2 = clazz2.getSuperclass();
                }
                return clazz1.getName();
            } catch (RuntimeException | ClassNotFoundException e) {
                return "java.lang.Object";
            }
        }
    };
    */

    private static ClassNode getClassTree(byte[] jbc) {
        ClassReader cr = new ClassReader(jbc);
        ClassNode clazzTree = new ClassNode();
        cr.accept(clazzTree, 0);
        return clazzTree;
    }

    private static void removeNops(ClassNode clazz) {
        for (MethodNode method : clazz.methods) {
            ListIterator<AbstractInsnNode> ops = method.instructions.iterator();
            while (ops.hasNext()) {
                AbstractInsnNode op = ops.next();
                if (op.getOpcode() == NOP || op.getOpcode() == -1) {
                    ops.remove();
                }
            }
        }
    }

    private static HashSet<String> removeEmptyMethods(ClassNode clazz) {
        HashSet<String> removedMethods = new HashSet<>();
        Iterator<MethodNode> methods = clazz.methods.iterator();
        while (methods.hasNext()) {
            MethodNode method = methods.next();

            for (AbstractInsnNode ins : method.instructions.toArray()) {
                int op = ins.getOpcode();
                if (op == -1) {
                    continue;
                }
                if (op == Opcodes.RETURN) {
                    System.out.println("Removing entire method: " + clazz.name + "." + method.name + method.desc);
                    //removedMethods.add(clazzTree.name + "." + method.name + method.desc);
                    removedMethods.add(method.name + method.desc);
                    methods.remove();
                }
                break;
            }
        }
        return removedMethods;
    }

    private static void removeEmptyExceptions(ClassNode clazz) {
        for (MethodNode method : clazz.methods) {
            Iterator<TryCatchBlockNode> blocks =  method.tryCatchBlocks.iterator();
            while (blocks.hasNext()) {
                TryCatchBlockNode block = blocks.next();
                if (block.start.getLabel().getOffset() == block.end.getLabel().getOffset() - 1) {
                    System.out.println(clazz.name + ":Empty try block!");
                }
                blocks.remove();
            }
        }
    }

    private static void writeClass(ClassNode clazzTree, String filename) {
        /*
        clazzTree.accept(specialCW);
        try (FileOutputStream out = new FileOutputStream(new File(filename))) {
            out.write(specialCW.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        // Sort of time consuming to create a new classloader each call
        // Perhaps you can initialize the classloader another way?
        // It's possible you're leaving behind deadcode?
        /*
        ClassWriter specialCW = new ClassWriter(0) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                try {
                    Class clazz1 = loader.loadClass(type1.replace('/', '.'));
                    Class clazz2 = loader.loadClass(type2.replace('/', '.'));
                    while (!clazz1.equals(clazz2)) {
                        clazz1 = clazz1.getSuperclass();
                        clazz2 = clazz2.getSuperclass();
                    }
                    return clazz1.getName();
                } catch (RuntimeException | ClassNotFoundException e) {
                    return "java.lang.Object";
                }
            }
        };
        */
        SpecialClassWriter specialCW = new SpecialClassWriter(0);
        clazzTree.accept(specialCW);

        /*
        ClassVisitor cv = new ClassVisitor(ASM6, specialCW) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return specialCW.visitMethod(access,name,desc,signature,exceptions);
            }
        };
        CheckClassAdapter check = new CheckClassAdapter(cv);
        try {
            clazzTree.accept(check);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            System.exit(1);
        }
        */

        try (FileOutputStream out = new FileOutputStream(new File(filename))) {
            out.write(specialCW.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int countArguments(String desc) {
        int count = 0;
        boolean inRef = false;
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) == ')') {
                break;
            }
            if (inRef && desc.charAt(i) != ';') {
                continue;
            } else if (inRef && desc.charAt(i) == ';') {
                inRef = false;
                continue;
            }
            switch (desc.charAt(i)) {
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                    count += 1;
                    break;
                case 'L':
                    count += 1;
                    inRef = true;
                    break;
                default: break;
            }
        }
        return count;
    }

    private static boolean isStackPushIns(int op) {
        int[] pushIns = {
            AALOAD, ACONST_NULL, ALOAD, ANEWARRAY,ARRAYLENGTH, BALOAD, BIPUSH, CALOAD, CHECKCAST,
            D2F, D2I, D2L, DADD, DALOAD, DCMPG, DCMPL, DCONST_0, DCONST_1, DDIV, DLOAD, DMUL, DNEG,
            DREM, DSUB, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, F2D, F2I, F2L, FADD, FALOAD,
            FCMPG, FCMPL, FCONST_0, FCONST_1, FCONST_2, FDIV, FLOAD, FMUL, FNEG, FREM, FSUB, GETFIELD,
            GETSTATIC, I2B, I2C, I2D, I2F, I2L, I2S, IADD, IALOAD, IAND, ICONST_0, ICONST_1, ICONST_2,
            ICONST_3, ICONST_4, ICONST_5, ICONST_M1, IDIV, ILOAD, IMUL, INEG, INSTANCEOF, IOR, IREM,
            ISHL, ISHR, ISUB, IUSHR, IXOR, JSR, L2D, L2F, L2I, LADD, LALOAD, LAND, LCMP, LCONST_0, LCONST_1,
            LDC, LDIV, LLOAD, LMUL, LNEG, LOR, LREM, LSHL, LSHR, LSUB, LUSHR, LXOR, MULTIANEWARRAY, NEW, NEWARRAY,
            SALOAD, SIPUSH, SWAP, INVOKEVIRTUAL, INVOKESTATIC
        };

        for (int i = 0; i < pushIns.length; i++) {
            if (pushIns[i] == op) {
                return true;
            }
        }
        return false;
    }

    public static void removeMethodFromClass(ClassNode clazz, HashSet<String> badMethods) {
        Iterator<MethodNode> it = clazz.methods.iterator();
        while (it.hasNext()) {
            MethodNode method = it.next();
            ListIterator<AbstractInsnNode> itInsn = method.instructions.iterator();
            while (itInsn.hasNext()) {
                AbstractInsnNode insn = itInsn.next();
                MethodInsnNode methodInsn = null;
                int argCount = -1;

                if (insn instanceof MethodInsnNode) {
                    methodInsn = (MethodInsnNode) insn;
                }

                if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                    argCount = countArguments(methodInsn.desc) + 1;
                } else if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                    argCount = countArguments(methodInsn.desc);
                }

                if (argCount > -1 && badMethods.contains(methodInsn.owner + "." + methodInsn.name + methodInsn.desc)) {
                    System.out.println("Removing call to method: " + methodInsn.owner + "." + methodInsn.name + methodInsn.desc +
                    " from: " + clazz.name + "." + method.name + method.desc);

                    // Remove the invoke* call
                    itInsn.remove();
                    if (itInsn.hasPrevious()) {
                        insn = itInsn.previous();  // Is there always a previous?
                    }

                    while (argCount > 0) {
                        // Add invoke to this ?
                        if (insn.getOpcode() == INVOKESTATIC) {
                            argCount += countArguments(((MethodInsnNode) insn).desc);
                        } else if (insn.getOpcode() == INVOKEVIRTUAL) {
                            argCount += countArguments(((MethodInsnNode) insn).desc) + 1;
                        }
                        if (isStackPushIns(insn.getOpcode())) {
                            System.out.println("\t" + insn.getOpcode());
                            argCount -= Util.OpcodeStackNetEffect.get(insn.getOpcode());
                            itInsn.remove();
                            insn = itInsn.previous();
                            //argCount--;
                        } else {
                            insn = itInsn.previous();
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        class RemoveBadMethodCallsVisitor extends ClassVisitor {
            private HashSet<String> badMethods;

            class RemoveCall extends MethodVisitor {
                /*
                public RemoveCall(int api, MethodVisitor mv) {
                    super(api, mv);
                }
                */
                public RemoveCall(int api) {
                    super(api);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                    if (badMethods.contains(name + desc)) {
                        System.out.println("Removing call to: " + owner + "." + name + desc);
                    }
                    //mv.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            }

            public RemoveBadMethodCallsVisitor(int api, ClassVisitor cv, HashSet<String> badMethods) {
                super(api, cv);
                this.badMethods = badMethods;
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                // Still visits this method specifically, but nothing else about it
                /*
                MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
                return new RemoveCall(ASM6, mv);
                */
                return null;
            }
        }

        // Possible that removing empty methods technically
        // deleted implementations of abstract methods..
        class NoEmptyMethodsTree extends ClassNode {
            public HashSet<String> deletedMethods = new HashSet<>();

            public NoEmptyMethodsTree(int api) {
                super(api);
            }

            @Override
            public void visitEnd() {
                // Could potentially just do a check such that:
                // if method name is 1 or 2 characters, safe to remove,
                // since no sane interface will do that
                /*
                if (!this.interfaces.isEmpty()) {
                    return;
                }
                */
                Iterator<MethodNode> methods = this.methods.iterator();
                while (methods.hasNext()) {
                    MethodNode method = methods.next();
                    if (method.name.length() > 2) {
                        // Assume this is an interface method
                        continue;
                    }

                    if ((method.access & ACC_ABSTRACT) != 0) {
                        continue;
                    }
                    // Abstract methods have to be implemented, even if that
                    // just means void


                    for (AbstractInsnNode ins : method.instructions.toArray()) {
                        int op = ins.getOpcode();
                        if (op == -1) {
                            continue;
                        }
                        if (op == Opcodes.RETURN) {
                            System.out.println("Removing entire method: " + this.name + "." + method.name + method.desc);
                            deletedMethods.add(this.name + "." + method.name + method.desc);
                            methods.remove();
                        }
                        break;
                    }
                }
            }
        }

        // TODO
        // Needs called on every single class, utilizing deleted methods from all
        // classes
        class NoDeadCallsTree extends ClassNode {
            private HashSet<String> deletedMethods;

            public NoDeadCallsTree(HashSet<String> deletedMethods) {
                super(ASM6);
                this.deletedMethods = deletedMethods;
                //System.out.println(this.deletedMethods);
            }

            @Override
            public void visitEnd() {
                removeMethodFromClass();
            }

            private void removeMethodFromClass() {
                if (deletedMethods.isEmpty()) {
                    return;
                }
                System.out.println("Removing methods calls from class");
                Iterator<MethodNode> it = this.methods.iterator();
                // Shouldn't remove interface calls at all
                while (it.hasNext()) {
                    MethodNode method = it.next();
                    ListIterator<AbstractInsnNode> itInsn = method.instructions.iterator();
                    while (itInsn.hasNext()) {
                        AbstractInsnNode insn = itInsn.next();
                        MethodInsnNode methodInsn = null;
                        int argCount = -1;

                        if (insn instanceof MethodInsnNode) {
                            methodInsn = (MethodInsnNode) insn;
                        } else {
                            continue;
                        }

                        if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL || insn.getOpcode() == INVOKEINTERFACE) {
                            argCount = countArguments(methodInsn.desc) + 1;
                        } else if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                            argCount = countArguments(methodInsn.desc);
                        }

                        // Should have printed twice?
                        if (argCount > -1 && deletedMethods.contains(methodInsn.owner + "." + methodInsn.name + methodInsn.desc)) {
                            System.out.println("Removing call to method: " + methodInsn.owner + "." + methodInsn.name + methodInsn.desc +
                            " from: " + this.name + "." + method.name + method.desc);

                            // Remove the invoke* call
                            itInsn.remove();
                            if (itInsn.hasPrevious()) {
                                insn = itInsn.previous();  // Is there always a previous?
                            }

                            while (argCount > 0) {
                                // Add invoke to this ?
                                if (insn.getOpcode() == INVOKESTATIC) {
                                    argCount += countArguments(((MethodInsnNode) insn).desc);
                                } else if (insn.getOpcode() == INVOKEVIRTUAL || insn.getOpcode() == INVOKEINTERFACE) {
                                    if (insn.getOpcode() == INVOKEINTERFACE) {
                                        System.out.println("Removing interface call to: " + methodInsn.owner + "." + methodInsn.name + methodInsn.desc);
                                    }
                                    argCount += countArguments(((MethodInsnNode) insn).desc) + 1;
                                }
                                //if (isStackPushIns(insn.getOpcode())) {
                                if (Util.OpcodeStackNetEffect.containsKey(insn.getOpcode())) {   // Excludes invoke*..
                                    System.out.println("\t" + insn.getOpcode());
                                    argCount -= Util.OpcodeStackNetEffect.get(insn.getOpcode());
                                    itInsn.remove();
                                    insn = itInsn.previous();
                                    //argCount--;
                                } else {
                                    insn = itInsn.previous();
                                }
                            }
                        }
                    }
                }
            }
        }

        class RemoveEmptyExceptionsVisitor extends ClassVisitor {
            class RemoveEmptyMethodException extends MethodVisitor {

                public RemoveEmptyMethodException(int api, MethodVisitor mv) {
                    super(api, mv);
                }

                @Override
                public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                    System.out.println(start + ":" + end);
                    /*
                    visitLabel(start);
                    visitLabel(end);
                    visitLabel(handler);
                    */
                    System.out.println(start.getOffset() + ":" + end.getOffset());
                    if (start.getOffset() == end.getOffset() - 1) {
                        System.out.println("Removing empty try catch block!");
                        return;
                    }
                    mv.visitTryCatchBlock(start, end, handler, type);
                }
            }

            public RemoveEmptyExceptionsVisitor(int api, ClassVisitor cv) {
                super(api, cv);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
                return new RemoveEmptyMethodException(ASM6, mv);
            }
        }

        class NoEmptyExceptionsTree extends ClassNode {
            public NoEmptyExceptionsTree() {
                super(ASM6);
            }

            @Override
            public void visitEnd() {
                for (MethodNode method : this.methods) {
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

        try {


            Deobfuscator deob = new Deobfuscator("/tmp/tmp.jar");
            deob.removeEmptyMethods()
                .removeCallsToMarkedMethods()
                .removeEmptyExceptions()
                .write();
            //System.out.println(deob.markedMethods);
            System.out.println("Done");
            Thread.sleep(10000);

            System.out.println("Remember to refresh tmp.jar!");
            Thread.sleep(1000);
            LinkedList<JavaClass> classes = new JarSearch("/tmp/tmp.jar").getClasses();
            HashSet<String> deletedMethods = new HashSet<>();
            LinkedList<NoEmptyMethodsTree> emptyMethodsTrees = new LinkedList<>();
            for (JavaClass jc : classes) {
                System.out.println(jc.getClassName() + ".class");
                ClassNode clazzTree = getClassTree(jc.getBytes());
                NoEmptyMethodsTree noEmptyMethodsTree = new NoEmptyMethodsTree(ASM6);
                clazzTree.accept(noEmptyMethodsTree);
                deletedMethods.addAll(noEmptyMethodsTree.deletedMethods);
                emptyMethodsTrees.add(noEmptyMethodsTree);
                /*
                NoDeadCallsTree noDeadCallsTree = new NoDeadCallsTree(noEmptyMethodsTree.deletedMethods);
                noEmptyMethodsTree.accept(noDeadCallsTree);
                NoEmptyExceptionsTree noEmptyExceptionsTree = new NoEmptyExceptionsTree();
                // Perform no empty methods on every class, placing all deleted methods in one big set
                // Then, loop through all the classes again, performing nodeadcalls and noempty exceptions, finally
                // writing the class from this
                noDeadCallsTree.accept(noEmptyExceptionsTree);
                SpecialClassWriter cw = new SpecialClassWriter(ClassWriter.COMPUTE_FRAMES);
                CheckClassAdapter check = new CheckClassAdapter(cw);
                //noEmptyExceptionsTree.accept(cw);
                noEmptyExceptionsTree.accept(check);

                // Maybe run the verifier?
                try (FileOutputStream out = new FileOutputStream(new File("/tmp/out/" + jc.getClassName() + ".class"))) {
                    out.write(cw.toByteArray());
                }
                */
                /*
                SpecialClassWriter cw = new SpecialClassWriter(0);
                noDeadCallsTree.accept(new RemoveEmptyExceptionsVisitor(ASM6 ,cw));
                try (FileOutputStream out = new FileOutputStream(new File("/tmp/out/" + jc.getClassName() + ".class"))) {
                    out.write(cw.toByteArray());
                }
                */
                /*
                System.out.println("Creating tree without empty methods!");
                clazzTree.accept(noEmptyMethodsTree);
                NoDeadCallsTree noDeadCallsTree = new NoDeadCallsTree(noEmptyMethodsTree.deletedMethods);
                System.out.println("Creating tree with no dead calls!");
                noEmptyMethodsTree.accept(noDeadCallsTree);
                */
                //HashSet<String> removedMethods = removeEmptyMethods(clazzTree);
                //clazzTree.accept(new RemoveBadMethodCallsVisitor(ASM6, new ClassWriter(0), removedMethods));
                /*
                ClassReader cr = new ClassReader(jc.getBytes());
                cr.accept(new RemoveBadMethodCallsVisitor(ASM6, new ClassWriter(0), removedMethods), 0);
                */
                //writeClass(clazzTree, "/tmp/out/" + jc.getClassName() + ".class");
                //writeClass(noDeadCallsTree, "/tmp/out/" + jc.getClassName() + ".class");
            }
            System.out.println(deletedMethods);
            for (NoEmptyMethodsTree tree : emptyMethodsTrees) {
                NoDeadCallsTree noDeadCallsTree = new NoDeadCallsTree(deletedMethods);
                tree.accept(noDeadCallsTree);
                //noEmptyMethodsTree.accept(noDeadCallsTree);
                NoEmptyExceptionsTree noEmptyExceptionsTree = new NoEmptyExceptionsTree();
                // Perform no empty methods on every class, placing all deleted methods in one big set
                // Then, loop through all the classes again, performing nodeadcalls and noempty exceptions, finally
                // writing the class from this
                noDeadCallsTree.accept(noEmptyExceptionsTree);
                SpecialClassWriter cw = new SpecialClassWriter(ClassWriter.COMPUTE_FRAMES);
                CheckClassAdapter check = new CheckClassAdapter(cw);
                //noEmptyExceptionsTree.accept(cw);
                noEmptyExceptionsTree.accept(check);

                // Maybe run the verifier?
                try (FileOutputStream out = new FileOutputStream(new File("/tmp/out/" + tree.name + ".class"))) {
                    out.write(cw.toByteArray());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
