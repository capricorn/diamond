package com.carp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;

public class ObjectTree {
    private Node rootNode;

    private class Node {
        String name;
        String path = "";
        Object value;
        Node parent;
        ArrayList<Node> children = new ArrayList<>();

        public Node(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public boolean isArray() {
            return value.getClass().isArray();
        }

        public boolean isPrimitive() {
            return value.getClass().isPrimitive() || value.getClass() == String.class;
        }

        public boolean isObject() {
            return !isArray() && !isPrimitive() && !value.getClass().getName().contains(".");
        }

        public void addChild(Node child) {
            child.parent = this;
            child.path = this.path + "." + this.name;
            children.add(child);
        }
    }

    public void printTree() {
        printTree(rootNode, 0);
    }

    private void printTree(Node node, int tab) {
        for (Node child : node.children) {
            if (child.value.getClass() == String.class) {
                printTab(tab);
                System.out.println(child.path + "." + child.name + ":" + child.value);
            }
            printTree(child, tab+1);
        }
    }

    public void writeTree() {
        try (PrintWriter out = new PrintWriter(new File("/tmp/out.txt"))) {
            writeTree(rootNode, out, 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeTree(Node node, PrintWriter out, int tab) {
        for (Node child : node.children) {
            if (child.value.getClass() == String.class) {
                //printTab(out, tab);
                out.write(child.path + "." + child.name + ":" + child.value);
                out.write('\n');
                //printTab(tab);
                //System.out.println(child.path + "." + child.name + ":" + child.value);
            }
            writeTree(child, out, tab+1);
            //printTree(child, tab+1);
        }
    }

    private void recurse(Node node, HashSet<Object> visited) {
        if (visited.contains(node.value)) return;
        visited.add(node.value);
        if (node.isArray()) {
            for (int i = 0; i < Array.getLength(node.value); i++) {
                Object item = Array.get(node.value, i);
                if (item == null) continue;
                Node itemNode = new Node(i+"", item);
                node.addChild(itemNode);
                /*
                if (itemNode.isPrimitive()) {
                    System.out.println("Adding child");
                    node.addChild(itemNode);
                    continue;
                }
                */
                recurse(itemNode, visited);
            }
        } else if (node.isObject()) {
            for (Field field : node.value.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(node.value);
                    if (fieldValue == null) continue;
                    Node fieldNode = new Node(field.getName(), fieldValue);
                    // Node is only added if it is a primitive..
                    /*
                    if (fieldNode.isPrimitive()) {
                        System.out.println("Adding child");
                        node.addChild(fieldNode);
                        continue;
                    }
                    */
                    node.addChild(fieldNode);
                    recurse(fieldNode, visited);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ObjectTree(Object object) {
        rootNode = new Node("client", object);
        HashSet<Object> visited = new HashSet<>();
        recurse(rootNode, visited);
    }

    private static void printTab(int tab) {
        for (int i = 0; i < tab; i++) {
            System.out.printf("  ");
        }
    }

    private static void printTab(PrintWriter out, int tab) {
        for (int i = 0; i < tab; i++) {
            out.write("  ");
            //System.out.printf("  ");
        }
    }
}
