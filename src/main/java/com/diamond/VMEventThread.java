package com.diamond;


import com.sun.jdi.Field;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.*;

public class VMEventThread implements Runnable {
    EventQueue queue;
    VirtualMachine vm;

    public VMEventThread(VirtualMachine vm) {
        this.vm = vm;
        this.queue = vm.eventQueue();
    }

    @Override
    public void run() {
        try {
            while (true) {
                EventSet events = queue.remove();
                EventIterator eventIter = events.eventIterator();
                while (eventIter.hasNext()) {
                    //System.out.println("New event");
                    Event event = eventIter.nextEvent();

                    if (event instanceof ModificationWatchpointEvent) {
                        ModificationWatchpointEvent modEvent = (ModificationWatchpointEvent) event;
                        System.out.println("Mod event: " + modEvent.valueToBe());
                    } else if (event instanceof AccessWatchpointEvent) {
                        System.out.println("Access request");
                        AccessWatchpointEvent accEvent = (AccessWatchpointEvent) event;
                        Field field = accEvent.field();
                        System.out.println(field.toString());
                        System.out.println(accEvent.valueCurrent());
                    } else if (event instanceof MethodEntryEvent) {
                        MethodEntryEvent methEvent = (MethodEntryEvent) event;
                        if (methEvent.method().name().equals("a")) {
                            System.out.println("Method: " + methEvent.toString());
                            System.out.println(methEvent.method().name());
                        }
                    } else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
                        return;
                    }
                }
                events.resume();
            }
        } catch (InterruptedException e) {

        }
    }
}
