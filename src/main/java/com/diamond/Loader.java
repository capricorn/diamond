package com.diamond;

import java.applet.Applet;
import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class Loader extends ClassLoader {
    private static final String baseUrl = "http://oldschool5.runescape.com/";
    private static final String gamepackFilePath = "/tmp/tmp.jar";
    private Hashtable<String, byte[]> gamepackClassBytes;
    private Hashtable<String, Class> gamepackClasses = new Hashtable<>();

    public Loader(String gamepackFilename) throws IOException {
        if (!new File(gamepackFilePath).isFile()) {
            downloadGamepack(gamepackFilename);
        }
        gamepackClassBytes = readJarClassBytes(gamepackFilePath);
    }

    public Applet getAppletInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return (Applet) loadClass("client").newInstance();
    }

    public byte[] getClassBytes(String name) {
        return gamepackClassBytes.get(name);
    }

    public Class findClass(String name) {
        try {
            if (gamepackClasses.contains(name)) {
                return gamepackClasses.get(name);
            }
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            byte[] b = gamepackClassBytes.get(name);

            // Attempting to load a class twice will cause an exception
            //System.out.println("Finding class: " + name);
            gamepackClasses.put(name, defineClass(name, b, 0, b.length));
            return gamepackClasses.get(name);
        }
    }

    private static Hashtable<String, byte[]> readJarClassBytes(String jarFilename) throws IOException {
        JarFile gamepack = new JarFile(jarFilename);
        Map<String, byte[]> map = gamepack.stream()
                .filter(p -> !p.getName().startsWith("META-INF"))
                .collect(Collectors.toMap(
                    keyMapper -> keyMapper.getName().replace(".class", ""),
                    valMapper -> {
                        try {
                            return Util.readInputStream(gamepack.getInputStream(valMapper));
                        } catch (IOException e) {
                            return null;
                        }
                    }));
        return new Hashtable<>(map);
    }

    private static void downloadGamepack(String filename) throws IOException {
        try (FileOutputStream urlFile = new FileOutputStream(gamepackFilePath);
             InputStream data = new URL(baseUrl + filename).openStream()) {

            int b;
            while ((b = data.read()) != -1) {
                urlFile.write(b);
            }
        }
    }
}
