package br.com.fiap.antidoto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger em /swagger-ui.html. O contrato que manda é o docs/api/openapi-mvp.yaml. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentacaoDaApi() {
        return new OpenAPI().info(new Info()
                .title("Antídoto — API Java")
                .version("1.0.0")
                .description("API feita só para o app Antídoto consumir, seguindo o contrato "
                        + "docs/api/openapi-mvp.yaml. Os dados de exemplo são fictícios."));
    }
}
