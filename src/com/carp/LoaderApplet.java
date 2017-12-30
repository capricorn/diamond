package com.carp;

import java.applet.Applet;
import java.applet.AppletStub;

public class LoaderApplet extends Applet {
    AppletStub stub;
    public LoaderApplet(AppletStub stub) {
        this.stub = stub;
    }

    public String getParameter(String name) {
        return stub.getParameter(name);
    }
}
