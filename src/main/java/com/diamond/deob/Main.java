package com.diamond.deob;

public class Main {
    public static void main(String[] args) {
        Deobfuscator deob = new Deobfuscator("/tmp/tmp.jar");
        deob.write();
    }
}
