package com.carp;

import com.sun.jdi.Bootstrap;
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

    public DiamondAPI(boolean debug) {
        try {
            gamepackParams = new GamepackParameters();
            String jar = gamepackParams.getInitialJar();
            app = new Loader(jar).getAppletInstance();

            if (debug) {
                new Thread(new FieldInfoGUI(app)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize API");
        }
    }

    public Applet getApplet() {
        return this.app;
    }

    public void run() {
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

    // Needs a custom data structure
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
