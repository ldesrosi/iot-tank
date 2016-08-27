package com.ibm.iot.tank;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

//Implement the CommandCallback class to provide the way in which you want the command to be handled
class MotorCommandCallback implements CommandCallback, Runnable {

        // A queue to hold & process the commands for smooth handling of MQTT messages
        private BlockingQueue<Command> queue = new LinkedBlockingQueue<Command>();

        private GpioController gpio = null;
        private GpioPinDigitalOutput pin = null;
        
        public MotorCommandCallback() {
          super();
          gpio = GpioFactory.getInstance();
          pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);
          pin.setShutdownOptions(true, PinState.LOW);
        }

        /**
        * This method is invoked by the library whenever there is command matching the subscription criteria
        */
        @Override
        public void processCommand(Command cmd) {
                try {
                        queue.put(cmd);
                } catch (InterruptedException e) {
                }
        }

        @Override
        public void run() {
                while(true) {
                        Command cmd = null;
                        try {
                                //In this sample, we just display the command
                                cmd = queue.take();
                                System.out.println("COMMAND RECEIVED = '" + cmd.getCommand() + "'\twith Payload = '" + cmd.getPayload() + "'");
                                pin.toggle();
                        } catch (InterruptedException e) {}
                }
        }
}
