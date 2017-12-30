package com.carp;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.tools.jdi.SocketAttachingConnector;

import javax.swing.*;
import java.applet.Applet;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DiamondClient {
    private static final String baseUrl = "http://oldschool5.runescape.com/";
    private static final String tmpJarFile = "/tmp/tmp.jar";

    // Why not just leave this to the loader?
    private Hashtable<String, byte[]> gamepackClasses;
    private GamepackParameters gamepackParams = new GamepackParameters();
    //private Loader loader;

    private Hashtable<String, byte[]> getGamepackClasses() {
        try {
            if (new File(tmpJarFile).exists()) {
                System.out.printf("Reading gamepack classes from %s.\n", tmpJarFile);
                return readJarFile(tmpJarFile);
            }
            return downloadJar(gamepackParams.getInitialJar());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read gamepack classes.");
        }
    }

    public void run() {
        Thread.currentThread().setName("Diamond");
        try {
            gamepackClasses = getGamepackClasses();

            JFrame clientWindow = new JFrame("\uD83D\uDC8E");

            Applet client = (Applet) new Loader(gamepackClasses).loadClass("client").newInstance();

            clientWindow.add(client);

            client.setStub(new LoaderStub(gamepackParams));
            client.setSize(gamepackParams.getAppletWidth(), gamepackParams.getAppletHeight());
            client.setVisible(true);
            client.init();
            client.start();

            clientWindow.setSize(gamepackParams.getWindowWidth(), gamepackParams.getWindowHeight());
            clientWindow.setLocationRelativeTo(null);
            clientWindow.setVisible(true);

            // Start debug field info gui window
            new Thread(new FieldInfoGUI(client)).start();
            DiamondAPI api = new DiamondAPI(client);
            //ObjectTree tree = new ObjectTree(client);

            // Maybe patch code somewhere that allows you to detect if game is running?
            /*
            VirtualMachine vm = attach();
            Thread.sleep(180000);
            // Examine the client tree here.
            pauseWorkingThreads(vm);
            Thread.sleep(1000);
            ObjectTree tree = new ObjectTree(client);
            //tree.printTree();
            tree.writeTree();
            //ObjectTree.printTree(client);
            //ClientTree tree = new ClientTree(client);
            System.out.println("Done!");
            //tree.printTree();
            resumeWorkingThreads(vm);
            */

            /*
            Thread.sleep(25000);
            String tmp = "";
            PrintWriter log = new PrintWriter("/tmp/chat.txt");
            while (true) {
                String[] chat = api.lastSaid();
                if (tmp == chat[1]) {
                    Thread.sleep(10);
                    continue;
                }
                // See if the message is the same by checking the object reference (hacky)
                tmp = chat[1];
                String username = chat[0].replaceAll("<[a-z0-9=\\/]+>", "");
                String text = chat[1].replaceAll("<[a-z0-9=\\/]+>", "");
                String message = String.format("%s %s\n", username, text);
                if (text.equals("$stop")) {
                    System.out.println("Stopping log.");
                    log.close();
                    break;
                }
                System.out.printf(message);
                log.write(message);
                Thread.sleep(10);
            }
            */
        } catch (Exception e) {
            e.printStackTrace();
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

    private static Hashtable<String, byte[]> downloadJar(String gamepack) throws IOException {
        System.out.printf("Downloading %s.\n", gamepack);
        downloadURL(baseUrl + gamepack, tmpJarFile);
        return readJarFile(tmpJarFile);
    }
}
