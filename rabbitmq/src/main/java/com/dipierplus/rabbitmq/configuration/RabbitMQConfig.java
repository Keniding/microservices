package com.dipierplus.rabbitmq.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "serviceQueue";
    public static final String PRODUCT_QUEUE_NAME = "productQueue";
    public static final String PRICE_REQUEST_QUEUE_NAME = "priceRequestQueue";
    public static final String BILLING_QUEUE_NAME = "billingQueue";
    public static final String EXCHANGE_NAME = "appExchange";
    public static final String ROUTING_KEY = "routing.key";
    public static final String PRODUCT_ROUTING_KEY = "product.created";
    public static final String PRICE_ROUTING_KEY = "product.price";
    public static final String CART_BILLING_ROUTING_KEY = "billing.cart";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Queue productQueue() {
        return new Queue(PRODUCT_QUEUE_NAME, true);
    }

    @Bean
    public Queue priceRequestQueue() {
        return new Queue(PRICE_REQUEST_QUEUE_NAME, true);
    }

    @Bean
    public Queue billingQueue() {
        return new Queue(BILLING_QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding productBinding(Queue productQueue, TopicExchange exchange) {
        return BindingBuilder.bind(productQueue).to(exchange).with(PRODUCT_ROUTING_KEY);
    }

    @Bean
    public Binding priceBinding(Queue priceRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(priceRequestQueue).to(exchange).with(PRICE_ROUTING_KEY);
    }

    @Bean
    public Binding billingBinding(Queue billingQueue, TopicExchange exchange) {
        return BindingBuilder.bind(billingQueue).to(exchange).with(CART_BILLING_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}