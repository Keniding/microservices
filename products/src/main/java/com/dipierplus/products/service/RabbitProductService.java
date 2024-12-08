package com.dipierplus.products.service;

import com.dipierplus.products.event.ProductPriceRequestEvent;
import com.dipierplus.products.event.ProductPriceResponseEvent;
import com.dipierplus.products.model.Product;
import com.dipierplus.products.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class RabbitProductService {

    private final RabbitTemplate rabbitTemplate;
    private final ProductRepository productRepository;

    @RabbitListener(queues = "product.price.request")
    public void handlePriceRequest(ProductPriceRequestEvent request) {
        BigDecimal price = getPriceById(request.getProductId());
        ProductPriceResponseEvent response = new ProductPriceResponseEvent(request.getProductId(), price);
        rabbitTemplate.convertAndSend("appExchange", "product.price.response", response);
    }

    private BigDecimal getPriceById(String productId) {
        return productRepository.findById(productId)
                .map(product -> new BigDecimal(String.valueOf(product.getPrice())))
                .orElse(BigDecimal.ZERO);
    }
}