package com.carp;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class ArrayInfoGUI extends AbstractTableModel implements Runnable {
    public static int TYPE = 0;
    public static int VALUE = 1;
    public static int INDEX = 2;
    private Object[][] data;
    private static String[] columnNames = {
            "type",
            "value",
            "index",
    };

    @Override
    public String getColumnName(int column) {
        if (column > getColumnCount()) {
            return "";
        }
        return columnNames[column];
    }

        // Assumed (currently) that passed array has 2 columns
    public ArrayInfoGUI(Object[][] data) {
        this.data = data;
        // Create a new data object with extra column with indices.
        // Then, copy data back over, or see if you can do this before it happens?
    }

    public void updateTable() {
    }

    @Override
    public void run() {
        JFrame frame = new JFrame("Array Viewer");
        JTable table = new JTable(this);
        table.addMouseListener(new ArrayInfoListener(table));
        frame.add(new JScrollPane(table));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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

    public void setValueAt(Object object, int row, int col) {
        data[row][col] = object;
        fireTableCellUpdated(row, col);
    }
}
