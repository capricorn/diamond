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

    public static void handleFieldClick(Object obj, int colCount) {
        if (obj == null) {
            return;
        }
        // If object is an array
        boolean isArray = obj.getClass().isArray();
        // If array has primitive elements
        boolean hasPrimitiveElements = isArray ? obj.getClass().getComponentType().isPrimitive(): false;
        // If class itself is primitive
        boolean isPrimitive = obj.getClass().isPrimitive();
        System.out.println(obj.getClass().getName());

        // Treat it as a field
        if (!isArray && !isPrimitive) {
            System.out.println("Not an array, and not primitive.");
            new Thread(new FieldInfoGUI(obj)).start();
            return;
        }

        if (isArray && hasPrimitiveElements) {
            System.out.println("Array with primitive elements.");
            Object[][] data = new Object[Array.getLength(obj)][colCount];

            for (int i = 0; i < Array.getLength(obj); i++) {
                System.out.println(obj.getClass().getComponentType().getName());
                System.out.println(Array.get(obj, i));
                data[i][0] = obj.getClass().getComponentType().getName();
                data[i][1] = Array.get(obj, i);
                data[i][ArrayInfoGUI.INDEX] = i;
            }
            new Thread(new ArrayInfoGUI(data)).start();
        } else if (obj.getClass().isArray()) {
            System.out.println("Array with non-primitive elements.");
            Object[] objects = (Object[]) obj;
            Object[][] data = new Object[objects.length][colCount];
            for (int i = 0; i < data.length; i++) {
                // Populate first column with just the objects
                data[i][0] = obj.getClass().getComponentType();
                data[i][1] = objects[i];
                data[i][ArrayInfoGUI.INDEX] = i;
            }
            new Thread(new ArrayInfoGUI(data)).start();
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
