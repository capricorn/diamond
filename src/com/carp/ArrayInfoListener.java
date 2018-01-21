package com.carp;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ArrayInfoListener implements MouseListener {
    private JTable table;

    public ArrayInfoListener(JTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();

            if (col == ArrayInfoGUI.VALUE) {
                FieldInfoGUI.handleFieldClick(table.getValueAt(row, col));
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
