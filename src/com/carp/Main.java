package com.carp;

public class Main {
    public static void main(String[] args) {
        DiamondAPI bot = DiamondAPI.init();
        new Thread(new FieldInfoGUI(bot.getApplet())).start();
        bot.runClient();
    }
}
