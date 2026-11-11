package br.com.fiap.antidoto.dto.avistamento;

import br.com.fiap.antidoto.model.LocalAvistamento;
import br.com.fiap.antidoto.model.TipoAnimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * POST e PUT /api/avistamentos. As mensagens são as do app. A "dataHora" no futuro
 * (além de 5 min) é conferida no service, com o relógio da API. Observação em
 * branco vira null.
 */
public record AvistamentoRequest(

        @NotNull(message = "Escolha o animal (ou “Não sei”)")
        TipoAnimal tipoAnimal,

        @NotNull(message = "Escolha onde você viu")
        LocalAvistamento local,

        @NotNull(message = "Informe a localização")
        @DecimalMin(value = "-34", message = "A localização precisa ser no Brasil")
        @DecimalMax(value = "6", message = "A localização precisa ser no Brasil")
        Double latitude,

        @NotNull(message = "Informe a localização")
        @DecimalMin(value = "-74", message = "A localização precisa ser no Brasil")
        @DecimalMax(value = "-34", message = "A localização precisa ser no Brasil")
        Double longitude,

        @NotNull(message = "Informe a data e a hora")
        Instant dataHora,

        @Size(max = 500, message = "A observação pode ter até 500 caracteres")
        String observacao

) {

    public AvistamentoRequest {
        observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }
}
