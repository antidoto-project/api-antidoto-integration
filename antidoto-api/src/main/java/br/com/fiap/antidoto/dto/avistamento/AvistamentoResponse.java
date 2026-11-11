package br.com.fiap.antidoto.dto.avistamento;

import br.com.fiap.antidoto.model.LocalAvistamento;
import br.com.fiap.antidoto.model.TipoAnimal;

import java.time.Instant;

/** Schema Avistamento do contrato. "atualizadoEm" fica null até a primeira edição. */
public record AvistamentoResponse(
        Long id,
        TipoAnimal tipoAnimal,
        LocalAvistamento local,
        Double latitude,
        Double longitude,
        Instant dataHora,
        String observacao,
        Instant criadoEm,
        Instant atualizadoEm
) {
}
