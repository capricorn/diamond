package com.carp;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Loader extends ClassLoader {
    private static final String baseUrl = "http://oldschool5.runescape.com/";
    private static final String tmpJarFile = "/tmp/tmp.jar";
    private Hashtable<String, byte[]> classFileData;
    public Hashtable<String, Class> loadedClasses;

    public Loader(String initialJar) {
        this.classFileData = getGamepackClasses(initialJar);
        loadedClasses = new Hashtable<>();
    }

    public Class findClass(String name) {
        System.out.println("Loading class: " + name);
        byte[] b = loadClassData(name);
        if (loadedClasses.contains(name)) {
            return loadedClasses.get(name);
        }
        loadedClasses.put(name, defineClass(name, b, 0, b.length));
        return loadedClasses.get(name);
    }

    private byte[] loadClassData(String name) {
        return classFileData.get(name);
    }
    private static Hashtable<String, byte[]> readJarFile(String jarFilename) throws IOException{
        JarFile gamepack = new JarFile(jarFilename);
        Hashtable<String, byte[]> classFileData = new Hashtable<>();
        Enumeration<JarEntry> gamepackEntries = gamepack.entries();
        int b;

        // Read the raw class files into byte arrays, which can be looked up according to class name.
        while (gamepackEntries.hasMoreElements()) {
            JarEntry entry = gamepackEntries.nextElement();
            if (entry.getName().startsWith("META-INF")) {
                continue;
            }
            InputStream entryDatastream = gamepack.getInputStream(entry);
            ByteArrayOutputStream classData = new ByteArrayOutputStream();

            while ((b = entryDatastream.read()) != -1) {
                classData.write(b);
            }

            classFileData.put(entry.getName().replace(".class", ""), classData.toByteArray());
        }

        return classFileData;
    }

    private static void downloadURL(String url, String filename) throws IOException {
        FileOutputStream urlFile = new FileOutputStream(filename);
        InputStream data = new URL(url).openStream();
        double urlSize = (double) (new URL(url)).openConnection().getContentLength();
        int b;

        for (double progress = 0; (b = data.read()) != -1; progress++) {
            Util.prettyDownload(progress, urlSize, 1000, url);
            urlFile.write(b);
        }
        System.out.printf("\nDownloaded %d bytes as %s.\n", (int) urlSize, filename);
        urlFile.close();
    }

    private static Hashtable<String, byte[]> downloadJar(String gamepack) {
        System.out.printf("Downloading %s.\n", gamepack);
        try {
            downloadURL(baseUrl + gamepack, tmpJarFile);
            return readJarFile(tmpJarFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download gamepack jar.");
        }
    }

    private Hashtable<String, byte[]> getGamepackClasses(String initialJar) {
        try {
            if (new File(tmpJarFile).exists()) {
                System.out.printf("Reading gamepack classes from %s.\n", tmpJarFile);
                return readJarFile(tmpJarFile);
            }
            return downloadJar(initialJar);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read gamepack classes.");
        }
    }
}
