package com.carp;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;

public class FieldInfoListener implements MouseListener {
    private JTable table;

    public FieldInfoListener(JTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();

            if (col == FieldInfoGUI.VALUE) {
                Object data = table.getValueAt(row, col);
                if (data.getClass().isArray()) {
                    int arrayLen = Array.getLength(data);
                    for (int i = 0; i < arrayLen; i++) {
                        System.out.println(i + ": " + Array.get(data, i));
                    }
                }
            }

            // Display the object in a new window.
            if (col == FieldInfoGUI.FIELD_NAME) {
                FieldInfoGUI.handleFieldClick(table.getValueAt(row, FieldInfoGUI.VALUE));
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
