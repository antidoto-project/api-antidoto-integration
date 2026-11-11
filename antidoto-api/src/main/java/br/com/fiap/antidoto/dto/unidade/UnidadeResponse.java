package br.com.fiap.antidoto.dto.unidade;

import br.com.fiap.antidoto.model.TipoSoro;
import br.com.fiap.antidoto.model.TipoUnidade;

import java.time.Instant;
import java.util.List;

/** Schema Unidade do contrato (lista e detalhe públicos). */
public record UnidadeResponse(
        Long id,
        String nome,
        TipoUnidade tipo,
        String endereco,
        String telefone,
        Double latitude,
        Double longitude,
        Boolean atende24h,
        List<TipoSoro> soros,
        Boolean ativa,
        Instant atualizadoEm
) {
}
