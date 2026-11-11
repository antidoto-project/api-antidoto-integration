package br.com.fiap.antidoto.dto.erro;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Formato único de erro (MVP-01 §7), o que o app lê. "campos" só aparece quando
 * há campos inválidos.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        List<CampoErroResponse> campos,
        String caminho,
        Instant timestamp
) {
}
