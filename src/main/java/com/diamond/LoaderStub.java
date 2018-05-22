package com.diamond;

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
import java.util.Iterator;

public class LoaderStub extends JFrame implements AppletStub, AppletContext {
    private GamepackParameters params;

    public LoaderStub(GamepackParameters params) {
        this.params = params;
    }

    public URL getDocumentBase() {
        try {
            return new URL(params.get("codebase"));
        } catch (MalformedURLException e) {
            return null;
        }
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
        System.out.printf("Getting parameter: %2s (%s)\n", name, params.get(name));
        return params.get(name);
    }

    public void appletResize(int width, int height) {
        System.out.printf("Resizing applet: %dx%d\n", width, height);
        setSize(width, height);
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
}
