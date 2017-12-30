package com.carp;

public class Main {
    public static void main(String[] args) {
        Thread.currentThread().setName("Main");
        DiamondClient diamondClient = new DiamondClient();
        diamondClient.run();
    }
}
