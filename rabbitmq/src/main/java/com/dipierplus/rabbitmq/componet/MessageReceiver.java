package com.dipierplus.rabbitmq.componet;

import org.springframework.stereotype.Component;

@Component
public class MessageReceiver {

    public void receiveMessage(Object message) {

        System.out.println("Received message: " + message);
    }
}

