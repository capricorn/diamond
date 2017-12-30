package com.carp;

import java.util.Formatter;

public class Util {
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
