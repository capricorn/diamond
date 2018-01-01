package com.carp;

import javax.swing.*;
import java.applet.Applet;

public class DiamondClient {
    public static void run(Applet client, GamepackParameters gamepackParams) {
        Thread.currentThread().setName("Diamond");
        JFrame clientWindow = new JFrame("\uD83D\uDC8E");
        clientWindow.add(client);

        client.setStub(new LoaderStub(gamepackParams));
        client.setSize(gamepackParams.getAppletWidth(), gamepackParams.getAppletHeight());
        client.setVisible(true);
        client.init();
        client.start();

        clientWindow.setSize(gamepackParams.getWindowWidth(), gamepackParams.getWindowHeight());
        clientWindow.setLocationRelativeTo(null);
        clientWindow.setVisible(true);
    }

    /*
    public void run() {
        try {
            Thread.currentThread().setName("Diamond");
            JFrame clientWindow = new JFrame("\uD83D\uDC8E");
            //GamepackParameters gamepackParams = new GamepackParameters();
            //Applet client = (Applet) new Loader(gamepackParams.getInitialJar()).loadClass("client").newInstance();

            clientWindow.add(client);

            client.setStub(new LoaderStub(gamepackParams));
            client.setSize(gamepackParams.getAppletWidth(), gamepackParams.getAppletHeight());
            client.setVisible(true);
            client.init();
            client.start();

            clientWindow.setSize(gamepackParams.getWindowWidth(), gamepackParams.getWindowHeight());
            clientWindow.setLocationRelativeTo(null);
            clientWindow.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
