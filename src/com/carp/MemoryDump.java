package com.carp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;

public class MemoryDump {
    private LinkedList<MemoryNode> stringList = new LinkedList<>();
    private LinkedList<MemoryNode> integerList = new LinkedList<>();
    private LinkedList<MemoryNode> floatList = new LinkedList<>();
    private LinkedList<MemoryNode> nullList = new LinkedList<>();

    private class MemoryNode {
        private Object value;
        private String path;

        public MemoryNode(Object value, String path) {
            this.value = value;
            this.path = path;
        }
    }
    /**
     * Populates itself by:
     * - Recursing through the tree of fields in object
     * - Storing any primitives (tree leafs)
     * @param object
     * The tree of fields searched for primitives
     */
    public MemoryDump(Object object, String name) {
        populateMemoryArray(object, name, new HashSet<>());
    }

    private void printNodeList(LinkedList<MemoryNode> list) {
        for (MemoryNode node : list) {
            System.out.println(node.path + ":" + node.value);
        }
    }

    private void writeNodeList(LinkedList<MemoryNode> list, String filename) throws IOException {
        try (FileWriter output = new FileWriter(new File(filename))) {
            for (MemoryNode node : list) {
                output.write(node.path + ":" + node.value + "\n");
            }
        }
    }

    public void writeStringList(String filename) throws IOException {
        writeNodeList(stringList, filename);
    }

    public void writeIntegerList(String filename) throws IOException {
        writeNodeList(integerList, filename);
    }

    public void writeFloatList(String filename) throws IOException {
        writeNodeList(floatList, filename);
    }

    public void writeNullList(String filename) throws IOException {
        writeNodeList(nullList, filename);
    }

    public void printStringList() {
        printNodeList(stringList);
    }

    public void printIntegerList() {
        printNodeList(integerList);
    }

    public void printFloatList() {
        printNodeList(floatList);
    }

    public void printNullList() {
        printNodeList(nullList);
    }

    public void populateMemoryArray(Object object, String path, HashSet<Object> visited) {
        MemoryNode node = new MemoryNode(object, path);

        if (object == null) {
            nullList.addLast(node);
            return;
        }

        if (visited.contains(object)) return;

        Class objClass = object.getClass();

        if (objClass.isArray()) {
            for (int i = 0; i < Array.getLength(object); i++) {
                visited.add(object);
                populateMemoryArray(Array.get(object, i), path + ".[" + i + "]", visited);
            }
            return;
        }

        if (objClass == Byte.class || objClass == Short.class || objClass == Integer.class || objClass == Long.class) {
            integerList.add(node);
            return;
        } else if (objClass == String.class) {
            stringList.add(node);
            return;
        } else if (objClass == Float.class || objClass == Double.class) {
            floatList.add(node);
            return;
        }

        /*
         * The applet uses a custom classloader. This code filters out any classes that were not specifically loaded
         * by the applet and excludes them from the output. Basically, any base java classes, like Thread, are loaded
         * by the system classloader, and so excluded. The only classes loaded by the applet are classes found in the
         * gamepack, which is what I'm interested in.
         *
         * The null check is included for classes loaded by the bootstrap classloader.
         */
        if (objClass.getClassLoader() == null || objClass.getClassLoader() == ClassLoader.getSystemClassLoader()) return;

        visited.add(object);

        for (Field field : objClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                populateMemoryArray(field.get(object), path + "." + field.getName(), visited);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
