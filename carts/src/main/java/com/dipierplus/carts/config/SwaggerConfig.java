package com.dipierplus.carts.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Facturación - Cart Module")
                        .description("Documentación de la API para el módulo de carrito de compras")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Desarrolladores UTP")
                                .email("kenidingh@gmail.com"))
                        .license(new License()
                                .name("Licencia MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentación externa"));
    }
}
