/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.smarttermoazureiot;

import com.microsoft.azure.sdk.iot.device.*;
import com.mycompany.smarttermoazureiot.events.DirectMethodCallback;
import com.mycompany.smarttermoazureiot.events.DirectMethodStatusCallback;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TimerTask;

/**
 *
 * @author Turma A
 */
public class Device extends javax.swing.JFrame {

    private static String connString = "HostName=SmartTermoAD.azure-devices.net;DeviceId=MyJavaDevice;SharedAccessKey=WJYK3kacbtGTyd8VYEXFMFyv7aNNLOVf0H3j2IEYXa4=";

    // Using the MQTT protocol to connect to IoT Hub
    private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
    private static DeviceClient client;
    private double distance;
    final GpioController gpio = GpioFactory.getInstance();
    GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);

    /**
     * Creates new form Device
     */
    Timer t = new Timer();
    TimerTask raspberry = new TimerTask() {
        // Trigger pin as OUTPUT
        GpioPinDigitalOutput sensorTriggerPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07);
        // Echo pin as INPUT
        GpioPinDigitalInput sensorEchoPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
            }
            sensorTriggerPin.high(); // Make trigger pin HIGH
            try {
                Thread.sleep((long) 0.01);// Delay for 10 microseconds
            } catch (InterruptedException ex) {
                Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
            }
            sensorTriggerPin.low(); //Make trigger pin LOW

            while (sensorEchoPin.isLow()) { //Wait until the ECHO pin gets HIGH

            }
            long startTime = System.nanoTime(); // Store the surrent time to calculate ECHO pin HIGH time.
            while (sensorEchoPin.isHigh()) { //Wait until the ECHO pin gets LOW

            }
            long endTime = System.nanoTime(); // Store the echo pin HIGH end time to calculate ECHO pin HIGH time.

            distance = ((((endTime - startTime) / 1e3) / 2) / 29.1);

            TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();

            telemetryDataPoint.distance = distance;
            if (distance < DirectMethodCallback.getDistance() && DirectMethodCallback.getAlarmState() == true) {
                pin.high();
            } else {
                pin.low();
            }
            String msgStr = telemetryDataPoint.serialize();
            Message msg = new Message(msgStr);
            
            // Add a custom application property to the message.
            // An IoT hub can filter on these properties without access to the message body.
            msg.setProperty("distanceAlert", (distance > 30) ? "true" : "false");

            System.out.println("Sending message: " + msgStr);

            Object lockobj = new Object();

            // Send the message.
            EventCallback callback = new EventCallback();
            client.sendEventAsync(msg, (IotHubEventCallback) callback, lockobj);

            synchronized (lockobj) {
                try {
                    lockobj.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println(distance);
        }
    };

    TimerTask sendMessage = new TimerTask() {
        @Override
        public void run() {
            TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();

            telemetryDataPoint.distance = distance;

            // Add the telemetry to the message body as JSON.
            String msgStr = telemetryDataPoint.serialize();
            Message msg = new Message(msgStr);

            // Add a custom application property to the message.
            // An IoT hub can filter on these properties without access to the message body.
            msg.setProperty("distanceAlert", (distance > 30) ? "true" : "false");

            System.out.println("Sending message: " + msgStr);

            Object lockobj = new Object();

            // Send the message.
            EventCallback callback = new EventCallback();
            client.sendEventAsync(msg, (IotHubEventCallback) callback, lockobj);

            synchronized (lockobj) {
                try {
                    lockobj.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    public Device() throws URISyntaxException, IOException {
        //initComponents();

        client = new DeviceClient(connString, protocol);
        client.open();
        
        client.subscribeToDeviceMethod(new DirectMethodCallback(), null, new DirectMethodStatusCallback(), null);
        
        t.scheduleAtFixedRate(raspberry, 200, 1000);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 561, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 464, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Device.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Device.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Device.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Device.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Device().setVisible(true);
                } catch (URISyntaxException ex) {
                    Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private static class EventCallback implements IotHubEventCallback {

        public void execute(IotHubStatusCode status, Object context) {
            System.out.println("IoT Hub responded to message with status: " + status.name());

            if (context != null) {
                synchronized (context) {
                    context.notify();
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
