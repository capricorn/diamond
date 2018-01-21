package com.carp;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.applet.Applet;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;

public class FieldInfoGUI extends AbstractTableModel implements Runnable {
    private Object[][] data;
    private Field[] fields;
    private static int CLASS_NAME = 0;
    public static int TYPE = 1;
    public static int FIELD_NAME = 2;
    public static int VALUE = 3;
    private static int INDEX = 4;
    private Object app;

    private static String[] columnNames = {
            "class",
            "type",
            "field",
            "value",
            "index",
    };

    public FieldInfoGUI(Object app) {
        this.app = app;
        fields = app.getClass().getDeclaredFields();
        // row x column
        data = new Object[fields.length][columnNames.length];
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /*
    @Override
    public boolean isCellEditable(int rowIndex, int colIndex) {
        if (colIndex == VALUE) {
            return true;
        }
        return false;
    }
    */

    private void updateTable() {
        for (int row = 0; row < getRowCount(); row++) {
            Field field = fields[row];
            field.setAccessible(true);
            // Set each row value, use as an index when viewing.
            setValueAt(row, row, INDEX);
            setValueAt(app.getClass().getName(), row, CLASS_NAME);
            setValueAt(field.getName(), row, FIELD_NAME);
            setValueAt(field.getType().getName(), row, TYPE);
            try {
                setValueAt((field.get(app) == null) ? "null" : field.get(app), row, VALUE);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                setValueAt("ERROR", row, VALUE);
            }
        }
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public static void handleFieldClick(Object obj) {
        if (obj == null) {
            return;
        }

        if (obj.getClass().isArray()) {
            new Thread(new ArrayInfoGUI(obj)).start();
        } else {
            new Thread(new FieldInfoGUI(obj)).start();
        }
    }

    // Still have to call start()
    public void run() {
        JFrame frame = new JFrame("Field Info");

        updateTable();
        //frame.add(new JTable(data, columnNames));
        JTable table = new JTable(this);
        table.addMouseListener(new FieldInfoListener(table));
        //JScrollPane pane = new JScrollPane(new JTable(this));
        JScrollPane pane = new JScrollPane(table);
        //frame.add(new JTable(this));
        frame.add(pane);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        System.out.println("Updating table..");
        while (true) {
            try {
                Thread.sleep(1000);
                updateTable();
            } catch (InterruptedException e) {
            }
        }
    }
}
