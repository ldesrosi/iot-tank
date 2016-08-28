package com.ibm.iot.comms;

import java.util.Properties;

//import com.google.gson.JsonObject;
//
//import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;

public class IoTManager {
  public static void main(String[] args) {
    Properties options = new Properties();
    try {
       options.load(IoTManager.class.getResourceAsStream("/iotf.properties"));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }

    DeviceClient myClient = null;
    try {
      //Instantiate the class by passing the properties file
      myClient = new DeviceClient(options);

      //Connect to the IBM IoT Foundation
      myClient.connect();
 

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1); 
    }

  }
}

