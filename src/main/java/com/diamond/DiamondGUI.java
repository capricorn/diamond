package com.diamond;

import javax.swing.*;
import java.awt.*;

public class DiamondGUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame("test");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JToolBar toolBar = new JToolBar("x", JToolBar.HORIZONTAL);
        //JButton button = new JButton("test");
        toolBar.setFloatable(false);
        toolBar.add(new JButton("<"));
        toolBar.add(new JButton(">"));
        toolBar.addSeparator();
        toolBar.add(new JButton("Memory Dump"));
        toolBar.addSeparator();
        toolBar.add(new JButton("Suspend"));
        frame.add(toolBar, BorderLayout.NORTH);
        JPanel pathPanel = new JPanel();
        pathPanel.add(new JLabel("Path"));
        JTextField pathField = new JTextField("client.a.x");
        pathField.setColumns(40);
        pathPanel.add(pathField);
        frame.add(pathPanel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(new JTable(new ArrayInfoGUI(new Object[][]{{1, 2, 3}, {1, 2, 3}}))));
        //frame.add(new JButton("hello"), BorderLayout.CENTER);

        frame.setSize(300,300);
        frame.pack();
        frame.setVisible(true);
    }
}
