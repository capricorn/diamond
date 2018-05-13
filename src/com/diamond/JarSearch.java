package com.diamond;

import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.classfile.Method;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarSearch {
    LinkedList<JavaClass> classes = new LinkedList<>();
    public JarSearch(String jarPath) {
        try {
            JarFile jar = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    classes.add(new ClassParser(jar.getInputStream(entry), entry.getName()).parse());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to open jar at " + jarPath);
        }
    }

    public LinkedList<JavaClass> getClasses() {
        return classes;
    }

    public void printMethodsOfType(String type) {
        for (JavaClass clazz : classes) {
            for (Method method : clazz.getMethods()) {
                String signature = method.getSignature();
                if (signature.contains("[L" + type)) {
                    System.out.println("[" + clazz.getClassName() + ".class]" + " " + method.getName() + ":" + signature);
                }
            }
        }
    }

    public void printMethods() {
        for (JavaClass clazz : classes) {
            for (Method method : clazz.getMethods()) {
                System.out.println(clazz.getClassName() + ":" + method.getSignature());
            }
        }
    }
}
