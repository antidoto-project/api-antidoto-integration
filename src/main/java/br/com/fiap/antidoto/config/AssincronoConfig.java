package br.com.fiap.antidoto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Liga o @Async: o resumo da ocorrência é gerado em segundo plano, depois que o
 * POST já respondeu (MVP-01 §9, o modo sem RabbitMQ). Usa o executor padrão do Spring Boot.
 */
@Configuration
@EnableAsync
public class AssincronoConfig {
}
