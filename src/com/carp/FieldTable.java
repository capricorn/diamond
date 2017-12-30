package com.carp;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class FieldTable extends AbstractTableModel {


    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }

    public void setValueAt(Object value, int row, int col) {
    }
}
