package com.dipierplus.rabbitmq.controller;

import com.dipierplus.rabbitmq.dto.MessageRequest;
import com.dipierplus.rabbitmq.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/messages")
@AllArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public void sendMessage(@RequestBody String message) {
        messageService.sendMessage("routing.key", message);
    }

    @PostMapping("/send")
    public void sendMessage(@RequestBody MessageRequest messageRequest) {
        messageService.sendMessage(messageRequest.getRoutingKey(), messageRequest.getMessage());
    }
}
