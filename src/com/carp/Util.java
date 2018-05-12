package com.carp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;

public class Util {
    public static byte[] readInputStream(InputStream input) throws IOException {
        ByteArrayOutputStream classData = new ByteArrayOutputStream();
        int b;
        while ((b = input.read()) != -1) {
            classData.write(b);
        }
        return classData.toByteArray();
    }

    public static void prettyDownload(double progress, double goal, int step, String filename) {
        if (progress % step == 0) {
            StringBuilder progressStr = new StringBuilder();
            Formatter progressFormat = new Formatter(progressStr);
            System.out.printf("\r");
            progressFormat.format("%3d %% -> %s",
                    (int) ((progress / goal) * 100.0), filename);
            progressFormat.flush();
            System.out.printf("%s", progressStr);
            progressStr.setLength(0);
        }
    }
}
