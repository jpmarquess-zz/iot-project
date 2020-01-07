/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.smarttermoazureiot.events;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;

/**
 *
 * @author Turma A
 */
public class DirectMethodCallback implements DeviceMethodCallback {

    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_NOT_DEFINED = 404;
    private static final int INVALID_PARAMETER = 400;
    public static int distance = 50;
    public static boolean alarm = true;

    @Override
    public DeviceMethodData call(String methodName, Object methodData, Object context) {
        DeviceMethodData deviceMethodData = null;
        String payload = new String((byte[]) methodData);

        switch (methodName) {
            case "setDistance": {
                int interval;
                try {
                    int status = METHOD_SUCCESS;
                    interval = Integer.parseInt(payload);
                    System.out.println(payload);
                    setDistance(interval);
                    deviceMethodData = new DeviceMethodData(status, "Comunicando com dispositivo..." + "\nAlterar distancia mínima..." + "\nDistancia mínima alterada para: " + interval);
                } catch (NumberFormatException e) {
                    int status = INVALID_PARAMETER;
                    deviceMethodData = new DeviceMethodData(status, "Invalid parameter " + payload);
                }

                break;
            }
            case "setAlarmeState": {
                boolean interval;
                try {
                    int status = METHOD_SUCCESS;
                    //interval = Integer.parseInt(payload);
                    interval = Boolean.parseBoolean(payload);
                    System.out.println(payload);
                    setAlarmState(interval);
                    deviceMethodData = new DeviceMethodData(status, "\nDistancia mínima:" + interval + "\nExecuted direct method " + methodName);
                } catch (NumberFormatException e) {
                    int status = INVALID_PARAMETER;
                    deviceMethodData = new DeviceMethodData(status, "Invalid parameter " + payload);
                }

                break;
            }
            default: {
                int status = METHOD_NOT_DEFINED;
                deviceMethodData = new DeviceMethodData(status, "Not defined direct method " + methodName);
            }
        }
        return deviceMethodData;
    }

    private void setDistance(int distance) {
        DirectMethodCallback.distance = distance;
    }

    public static int getDistance() {
        return distance;
    }

    public static void setAlarmState(boolean alarm) {
        DirectMethodCallback.alarm = alarm;
    }
    
    public static boolean getAlarmState() {
        return alarm;
    }
}
