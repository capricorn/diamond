package com.carp;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Field;

public class FieldInfoGUI extends AbstractTableModel implements Runnable {
    private Object[][] data;
    private Field[] fields;
    private static int INDEX = 0;
    private static int CLASS_NAME = 1;
    public static int TYPE = 2;
    public static int FIELD_NAME = 3;
    public static int VALUE = 4;

    private Object app;

    private static String[] columnNames = {
            "Index",
            "Class",    // Maybe set to actual object ref?
            "Type",
            "Field",
            "Value",
    };

    // rename from app
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

    private void updateTable() {
        for (int row = 0; row < getRowCount(); row++) {
            Field field = fields[row];
            field.setAccessible(true);
            // Set each row value, use as an index when viewing.
            setValueAt(row, row, INDEX);
            setValueAt(field.getDeclaringClass(), row, CLASS_NAME);
            setValueAt(field.getName(), row, FIELD_NAME);
            setValueAt(field.getType().getName(), row, TYPE);
            try {
                setValueAt((field.get(app) == null) ? "null" : field.get(app), row, VALUE);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
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
        if (value == null) {
            return;
        }

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
                // Is this really desirable behavior?
                // How can you synchronize this with manual updates?
                // Maybe pause updating table while editing is occurring?
                Thread.sleep(1000);
                updateTable();
            } catch (InterruptedException e) {
            }
        }
    }
}
