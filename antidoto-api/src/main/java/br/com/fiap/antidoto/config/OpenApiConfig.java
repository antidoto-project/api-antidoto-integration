package br.com.fiap.antidoto.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger em /swagger-ui.html. O contrato que manda é o docs/api/openapi-mvp.yaml.
 * "Authorize" recebe o token do login; as rotas públicas aparecem sem cadeado.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BEARER = "bearerAuth";

    @Bean
    public OpenAPI documentacaoDaApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Antídoto — API Java")
                        .version("1.0.0")
                        .description("API feita só para o app Antídoto consumir, seguindo o contrato "
                                + "docs/api/openapi-mvp.yaml. Os dados de exemplo são fictícios. "
                                + "Usuário de teste: cidadao@sospeconha.dev / Teste@123."))
                .components(new Components().addSecuritySchemes(ESQUEMA_BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER));
    }
}
