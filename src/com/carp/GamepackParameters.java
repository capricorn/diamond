package com.carp;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Scanner;

public class GamepackParameters {
    private Hashtable<String, String> gamepackParams = new Hashtable<>();
    // Will change update-to-update
    private static String DEFAULT_WORLD = "8";

    public GamepackParameters() {
        try (Scanner paramInput = new Scanner(new URL("http://oldschool5.runescape.com/jav_config.ws").openStream())) {
            while (paramInput.hasNext()) {
                /*
                Parameter format
                    param=KEY=DATA
                    msg=KEY=DATA
                    KEY=DATA
                Note DATA can contain '=' as a character, which makes parsing more difficult.
                 */
                String param = paramInput.nextLine();
                String type = param.substring(0, param.indexOf("="));
                if (type.equals("param") || type.equals("msg")) {
                    String str = param.substring(param.indexOf("=")+1);
                    String key = str.substring(0, str.indexOf("="));
                    String data = str.substring(str.indexOf("=")+1);
                    gamepackParams.put(key, data);
                } else {
                    gamepackParams.put(type, param.substring(param.indexOf("=")+1));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve parameters from site.");
        }
    }

    public String get(String key) {
        return gamepackParams.get(key);
    }

    public String getInitialJar() {
        return get("initial_jar");
    }

    public int getDefaultWorld() {
        return Integer.parseInt(get(DEFAULT_WORLD));
    }

    public int getWindowWidth() {
        return Integer.parseInt(get("window_preferredwidth"));
    }

    public int getWindowHeight() {
        return Integer.parseInt(get("window_preferredheight"));
    }

    public int getAppletWidth() {
        return Integer.parseInt(get("applet_minwidth"));
    }

    public int getAppletHeight() {
        return Integer.parseInt(get("applet_minheight"));
    }
}
