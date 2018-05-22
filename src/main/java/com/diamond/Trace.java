package com.diamond;

import com.sun.jdi.Field;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;

import java.io.IOException;

public class Trace {
    private static void setFieldTrace(VirtualMachine vm, String className, String fieldName) {
        try {
            Field field = vm.classesByName(className).get(0).fieldByName(fieldName);
            System.out.println(field.toString());
            AccessWatchpointRequest accReq = vm.eventRequestManager().createAccessWatchpointRequest(field);
            ModificationWatchpointRequest modReq = vm.eventRequestManager().createModificationWatchpointRequest(field);
            MethodEntryRequest methReq = vm.eventRequestManager().createMethodEntryRequest();
            methReq.addClassFilter("cc");
            methReq.enable();
            /*
            accReq.enable();
            modReq.enable();
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Only run if diamond client is running already
        try {
            VirtualMachine vm = DiamondAPI.attach();
            //setFieldTrace(vm, "im", "bv");
            setFieldTrace(vm, "im", "ew");
            /*
            System.out.println(vm.process());
            new Thread(new VMProcIOThread(vm.process().getErrorStream()));
            new Thread(new VMProcIOThread(vm.process().getInputStream()));
            */
            new Thread(new VMEventThread(vm)).start();
            vm.resume();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        }
    }
}
