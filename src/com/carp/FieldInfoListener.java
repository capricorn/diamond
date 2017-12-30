package com.carp;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import java.util.Arrays;

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
                Class clazz = data.getClass();
                String output = null;
                System.out.println(data);

                //System.out.println(clazz.getComponentType().getName());
                if (clazz.isArray() && clazz.getComponentType().isPrimitive()) {
                    switch (clazz.getComponentType().toString()) {
                        case "int":
                            output = Arrays.toString((int[])data);
                            break;
                        case "String":
                            output = Arrays.toString((int[])data);
                            break;
                    }
                    System.out.println(output);
                    //System.out.println(data.getClass().getComponentType());
                } else if (clazz.getComponentType().getName().equals("java.lang.String")) {
                    System.out.println(Arrays.toString((String[])data));
                }
            }

            // Display the object in a new window.
            if (col == FieldInfoGUI.FIELD_NAME) {
                FieldInfoGUI.handleFieldClick(table.getValueAt(row, FieldInfoGUI.VALUE), table.getColumnCount());
            }
                /*
                Object obj = table.getValueAt(row, FieldInfoGUI.VALUE);
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
                    Object[][] data = new Object[Array.getLength(obj)][table.getColumnCount()];

                    for (int i = 0; i < Array.getLength(obj); i++) {
                        System.out.println(obj.getClass().getComponentType().getName());
                        System.out.println(Array.get(obj, i));
                        data[i][0] = obj.getClass().getComponentType().getName();
                        data[i][1] = Array.get(obj, i);
                    }
                    new Thread(new ArrayInfoGUI(data)).start();
                } else if (obj.getClass().isArray()) {
                    System.out.println("Array with non-primitive elements.");
                    Object[] objects = (Object[]) obj;
                    Object[][] data = new Object[objects.length][table.getColumnCount()];
                    for (int i = 0; i < data.length; i++) {
                        // Populate first column with just the objects
                        data[i][0] = obj.getClass().getComponentType();
                        data[i][1] = objects[i];
                    }
                    new Thread(new ArrayInfoGUI(data)).start();
                }
            }
            */

            if (col == FieldInfoGUI.TYPE) {
                // Just check which classloader loaded it (should be Loader())
                // Bit of a hack..
                //Object data = table.getValueAt(row, FieldInfoGUI.TYPE);
                /*
                Class clazz = (Class) table.getValueAt(row, FieldInfoGUI.TYPE);
                System.out.println(clazz.getName());
                */
                /*
                Class clazz = data.getClass();

                if (clazz.isArray()) {
                    System.out.println(Arrays.toString((Object[])data));
                }
                */
                /*
                Object value = table.getValueAt(row, FieldInfoGUI.VALUE);
                // Have differing classloaders.. this is a good thing!
                if (data.getClass().getClassLoader() != this.getClass().getClassLoader()) {
                    //new Thread(new FieldInfoGUI(data)).start();
                    new Thread(new FieldInfoGUI(value)).start();
                }
                */
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
