package com.diamond;

public class Main {
    public static void main(String[] args) {
        DiamondAPI bot = new DiamondAPI(true);
        System.out.println("Version: " + bot.getGamepackVersion());
        bot.run();
    }
}
