package com.carp;

import javax.swing.*;
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.*;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class LoaderStub extends JFrame implements AppletStub, AppletContext {
    Hashtable<String, String> params;
    public LoaderStub(Hashtable<String, String> params) {
        this.params = params;
    }

    public URL getDocumentBase() {
        System.out.println("Get doc base");
        try {
            //return new File("/Users/cap/prog/java/diamond/data/gamepack_5961714.jar").toURI().toURL();
            return new URL("http://oldschool5.runescape.com/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public URL getCodeBase() {
        return getDocumentBase();
    }

    public Iterator getStreamKeys() {
        throw new UnsupportedOperationException();
    }

    public void showStatus(String s) {
        throw new UnsupportedOperationException();
    }

    public boolean isActive() {
        return true;
    }

    public void showDocument(URL u) {
        return;
    }

    public String getParameter(String name) {
        System.out.printf("Getting parameter: %s (%s)\n", name, params.get(name));
        return params.get(name);
    }

    // Needs testing
    public void appletResize(int width, int height) {
        System.out.printf("Resizing applet: %dx%d ", width, height);
        setSize(width, height);
        return;
    }

    public AudioClip getAudioClip(URL u) {
        throw new UnsupportedOperationException();
    }
    public InputStream getStream(String key) {
        throw new UnsupportedOperationException();
    }

    public void setStream(String key, InputStream stream) {
        throw new UnsupportedOperationException();
    }

    public Image getImage(URL u) {
        throw new UnsupportedOperationException();
    }

    public void showDocument(URL u, String s) {
        return;
    }

    public Applet getApplet(String name) {
        throw new UnsupportedOperationException();
    }

    public Enumeration getApplets() {
        throw new UnsupportedOperationException();
    }

    public AppletContext getAppletContext() {
        System.out.println("Get applet context");
        return this;
    }

    public void run(Hashtable<String, byte[]> classFileData) {
        setTitle("Diamond Client");

        setSize(500, 500);
        Loader loader = new Loader(classFileData);

        try {
            Applet client = (Applet) loader.loadClass("client").newInstance();
            System.out.println("Applet loaded.");
            add(client);
            client.setStub(this);
            client.init();
            client.start();
            client.setVisible(true);
            pack();
            client.setSize(1000,1000);
            //setVisible(true);
            //client.setSize(500, 500);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Had a problem trying to run the applet..");
        }
    }
}
