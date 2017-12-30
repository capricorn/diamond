package com.carp;

import java.util.Hashtable;

public class Loader extends ClassLoader {
    private Hashtable<String, byte[]> classFileData;
    public Hashtable<String, Class> loadedClasses;

    public Loader(Hashtable<String, byte[]> classFileData) {
        this.classFileData = classFileData;
        loadedClasses = new Hashtable<>();
    }

    public Class findClass(String name) {
        System.out.println("Loading class: " + name);
        /* Fancy printing
        String msg = "Loaded class: ";
        System.out.printf(msg + name);
        System.out.println("Loaded class: " + name);
        for (int i = 0; i < msg.length() + name.length(); i++) {
            System.out.printf("\b");
        }
        */
        //System.out.println(name);
        byte[] b = loadClassData(name);
        /*
        Class _class = defineClass(name, b, 0, b.length);
        loadedClasses.put(name, _class);
        return _class;
        */
        //return defineClass(name, b, 0, b.length);
        loadedClasses.put(name, defineClass(name, b, 0, b.length));
        return loadedClasses.get(name);
    }

    private byte[] loadClassData(String name) {
        return classFileData.get(name);
    }
}
