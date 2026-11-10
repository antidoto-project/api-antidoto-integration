package br.com.fiap.antidoto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Relógio da API (MVP-01 §3): as regras de tempo (3 h da RN-U5, 72 h da RN-G4)
 * usam este Clock, e os testes trocam por um Clock fixo.
 */
@Configuration
public class RelogioConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }
}
