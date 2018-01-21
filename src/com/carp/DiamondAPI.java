package com.carp;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.tools.jdi.SocketAttachingConnector;

import java.applet.Applet;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class DiamondAPI {
    private Applet app;
    private GamepackParameters gamepackParams;
    private static int HP_INDEX = 3;
    private static HashMap<String, String> hookMap = new HashMap<String, String>() {
        {
            put("cc_owner", "np");
            put("cc_topic", "nl");
            put("game_socket", "ej.a.a");
            put("last_said", "hh.0");
            put("current_world", "ci.w.s.z.429.1.br");
            put("fc_chat_user", "ci.w.s.z.162.44.ey.0.br");
            put("fc_chat_msg", "ci.w.s.z.162.44.ey.1.br");
            put("object_under_cursor", "jh.1");
        }
    };

    private DiamondAPI(Applet app, GamepackParameters gamepackParams) {
        this.app = app;
        this.gamepackParams = gamepackParams;
    }

    public Applet getApplet() {
        return this.app;
    }

    public static DiamondAPI init() {
        try {
            GamepackParameters gamepackParams = new GamepackParameters();
            Applet client = (Applet) new Loader(gamepackParams.getInitialJar()).loadClass("client").newInstance();
            return new DiamondAPI(client, gamepackParams);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize API");
        }
    }

    public void runClient() {
        // Could probably just be integrated here..
        DiamondClient.run(app, gamepackParams);
    }

    private Object getHook(String key) {
        String hookPath[] = hookMap.get(key).split("\\.");
        Object object = app;
        for (String hookObj : hookPath) {
            try {
                Integer arrayIndex = Integer.parseInt(hookObj);
                // Assumed that the object we fetched is an array
                object = Array.get(object, arrayIndex);
            } catch (NumberFormatException e) {
                object = getDeclaredField(object, hookObj);
            } catch (NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return object;
    }

    public String getObjectUnderCursor() {
        return (String) getHook("object_under_cursor");
    }

    public String getWorld() {
        return (String) getHook("current_world");
    }

    public int[] getCurrentStats() {
        return (int[]) getDeclaredField(app, "ie");
    }

    public String[] lastSaid() {
        String username = (String) getHook("fc_chat_user");
        String message = (String) getHook("fc_chat_msg");
        return new String[] { username, message };
    }

    public Socket getSocket() {
        return (Socket) getHook("game_socket");
    }

    public int getHP() {
        int[] stats = getCurrentStats();
        return stats[HP_INDEX];
    }

    public String getClanChat() {
        return (String) getHook("cc_owner");
    }

    private void setDeclaredField(Object parent, String field, Object value) {
        try {
            Field f = parent.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(parent, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object getDeclaredField(Object parent, String field) {
        try {
            Field f = parent.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(parent);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    // Used for examining the client tree at a given state.
    private void pauseWorkingThreads(VirtualMachine vm) {
        try {
            for (ThreadReference thread : vm.allThreads()) {
                if (!thread.name().equals("Diamond") && !thread.name().equals("JDI Target VM Interface")) {
                    thread.suspend();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resumeWorkingThreads(VirtualMachine vm) {
        for (ThreadReference thread : vm.allThreads()) {
            thread.resume();
        }
    }

    public static VirtualMachine attach() throws IOException, IllegalConnectorArgumentsException {
        for (AttachingConnector conn : Bootstrap.virtualMachineManager().attachingConnectors()) {
            if (conn instanceof SocketAttachingConnector) {
                Map<String, Connector.Argument> args = conn.defaultArguments();
                args.get("hostname").setValue("localhost");
                args.get("port").setValue("5739");
                return conn.attach(args);
            }
        }
        throw new IOException("Failed to locate socket attaching connector.");
    }
}
