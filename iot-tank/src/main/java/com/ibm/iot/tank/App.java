package com.ibm.iot.tank;

import java.util.Properties;

//import com.google.gson.JsonObject;
//
//import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;
//
//import com.pi4j.io.gpio.*;
//import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
//import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class App {
  public static void main(String[] args) {
    Properties options = new Properties();
    try {
       options.load(App.class.getResourceAsStream("/iotf.properties"));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }

    DeviceClient myClient = null;
    try {
      //Instantiate the class by passing the properties file
      myClient = new DeviceClient(options);

      //Pass the above implemented CommandCallback as an argument to this device client
      MotorCommandCallback callback = new MotorCommandCallback();
      Thread callbackThread = new Thread(callback);

      myClient.setCommandCallback(callback);
      callbackThread.start();

      //Connect to the IBM IoT Foundation
      myClient.connect();
 
      final SensorMonitor sensor = new SensorMonitor(myClient);
      //  GpioController gpio = GpioFactory.getInstance();

      //  GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04); //, PinPullResistance.PULL_DOWN);
      //  pin.setShutdownOptions(true);

      //  while (true) {
//	   System.out.println("Pin is:" + (pin.isHigh()?" High":" Low"));
 //          Thread.sleep(1000);
  //      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1); 
    }

  }
}

