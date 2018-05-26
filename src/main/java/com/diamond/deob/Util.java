package com.diamond.deob;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.JavaClass;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class Util {
    static int countArguments(String desc) {
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

    public static Map<Integer, Integer> OpcodeStackNetEffect = new HashMap<Integer, Integer>() {
        {
            put(AALOAD, -1);
            put(AASTORE, -3);
            put(ACONST_NULL, 1);
            put(ALOAD, 1);
            put(ANEWARRAY, 0);
            put(ARETURN, -1);
            put(ARRAYLENGTH, 0);
            put(ASTORE, -1);
            put(ATHROW, 0);
            put(BALOAD, -1);
            put(BASTORE, -3);
            put(BIPUSH, 1);
            put(CALOAD, -1);
            put(CASTORE, -3);
            put(CHECKCAST, 0);
            put(D2F, 0);
            put(D2I, 0);
            put(D2L, 0);
            put(DADD, -1);
            put(DALOAD, -1);
            put(DASTORE, -3);
            put(DCMPG, -1);
            put(DCMPL, -1);
            put(DCONST_0, 1);
            put(DCONST_1, 1);
            put(DDIV, -1);
            put(DLOAD, 1);
            put(DMUL, -1);
            put(DNEG, 0);
            put(DREM, -1);
            put(DRETURN, -1);
            put(DSTORE, -1);
            put(DSUB, -1);
            put(DUP, 1);
            put(DUP_X1, 1);
            // What to do about DUP_2*?
            put(F2D, 0);
            put(F2I, 0);
            put(F2L, 0);
            put(FADD, -1);
            put(FALOAD, -1);
            put(FASTORE, -3);
            put(FCMPG, -1);
            put(FCMPL, -1);
            put(FCONST_0, 1);
            put(FCONST_1, 1);
            put(FCONST_2, 1);
            put(FDIV, -1);
            put(FLOAD, 1);
            put(FMUL, -1);
            put(FNEG, 0);
            put(FREM, -1);
            put(FRETURN, -1);
            put(FSTORE, -1);
            put(FSUB, -1);
            put(GETFIELD, 0);
            put(GETSTATIC, 1);
            put(I2B, 0);
            put(I2C, 0);
            put(I2D, 0);
            put(I2F, 0);
            put(I2L, 0);
            put(I2S, 0);
            put(IADD, -1);
            put(IALOAD, -1);
            put(IAND, -1);
            put(IASTORE, -3);
            put(ICONST_0, 1);
            put(ICONST_1, 1);
            put(ICONST_2, 1);
            put(ICONST_3, 1);
            put(ICONST_4, 1);
            put(ICONST_5, 1);
            put(IDIV, -1);
            // Do we want these branch instructions to be counted?
            put(IF_ACMPEQ, -2);
            put(IF_ACMPNE, -2);
            put(IF_ICMPLT, -2);
            put(IF_ICMPGT, -2);
            put(IF_ICMPEQ, -2);
            put(IF_ICMPGE, -2);
            put(IF_ICMPLE, -2);
            put(IF_ICMPNE, -2);
            put(IFEQ, -1);
            put(IFNE, -1);
            put(IFLT, -1);
            put(IFGE, -1);
            put(IFGT, -1);
            put(IFLE, -1);
            put(IFNONNULL, -1);
            put(IFNULL, -1);
            put(ILOAD, 1);
            put(IMUL, -1);
            put(INEG, 0);
            put(INSTANCEOF, 0);
            // How to deal with invokedynamic, invokeinterface, invokespecial?
            // invokestatic / invokedynamic are variable arguments
            // Don't believe invokedynamic exists
            put(IOR, -1);
            put(IREM, -1);
            put(IRETURN, -1);   // Technically, empties stack?
            put(ISHL, -1);
            put(ISHR, -1);
            put(ISTORE, -1);
            put(ISUB, -1);
            put(IUSHR, -1);
            put(IXOR, -1);
            put(ICONST_M1, 1);
            put(JSR, 1);    // Should this be included?
            put(L2D, 0);
            put(L2F, 0);
            put(L2I, 0);
            put(LADD, -1);
            put(LALOAD, -1);
            put(LAND, -1);
            put(LASTORE, -3);
            put(LCMP, -1);
            put(LCONST_0, 1);
            put(LCONST_1, 1);
            put(LDC, 1);
            put(LDIV, -1);
            put(LLOAD, 1);
            put(LMUL, -1);
            put(LNEG, 0);
            put(LOOKUPSWITCH, -1);
            put(LOR, -1);
            put(LREM, -1);
            put(LRETURN, -1);
            put(LSHL, -1);
            put(LSHR, -1);
            put(LSTORE, -1);
            put(LSUB, -1);
            put(LUSHR, -1);
            put(LXOR, -1);
            put(MONITORENTER, -1);
            put(MONITOREXIT, -1);
            // multianewarray? pops 'dimensions' times
            put(NEW, 1);
            put(NEWARRAY, 0);
            put(POP, -1);
            put(POP2, -2);
            put(PUTFIELD, -2);
            put(PUTSTATIC, -1);
            put(SALOAD, -1);
            put(SASTORE, -3);
            put(SIPUSH, 1);
            put(SWAP, 0);
            put(TABLESWITCH, -1);
            // wide?
        }
    };

    // Just remember to ignore already visited.
    public static HashSet<String> getClassRefs(String className) {
        try {
            HashSet<String> refs = new HashSet<>();
            JavaClass jc = new ClassParser("/tmp/gp/" + className + ".class").parse();
            for (Constant con : jc.getConstantPool().getConstantPool()) {
                if (con == null) {
                    continue;
                }
                if (con.getTag() == Const.CONSTANT_Class) {
                    String refClassName = (String) ((ConstantClass) con).getConstantValue(jc.getConstantPool());
                    if (!className.equals(refClassName) &&
                            !refClassName.startsWith("java") && !refClassName.startsWith("[") &&
                            !refClassName.startsWith("netscape")) {
                        refs.add(refClassName);
                    }
                }
            }
            return refs;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static void removeMethod(ClassNode clazz, String methodDescriptor) {
        Iterator<MethodNode> methods = clazz.methods.iterator();

        while (methods.hasNext()) {
            MethodNode method = methods.next();

            if ((clazz.name + "." + method.name + method.desc).equals(methodDescriptor)) {
                methods.remove();
                return;
            }
        }
    }
}
