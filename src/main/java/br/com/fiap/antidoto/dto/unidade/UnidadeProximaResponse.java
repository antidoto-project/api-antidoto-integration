package br.com.fiap.antidoto.dto.unidade;

import br.com.fiap.antidoto.model.TipoSoro;
import br.com.fiap.antidoto.model.TipoUnidade;

import java.util.List;

/** Schema UnidadeProxima do contrato: o que o resultado do SOS mostra. */
public record UnidadeProximaResponse(
        Long id,
        String nome,
        TipoUnidade tipo,
        String endereco,
        String telefone,
        Double latitude,
        Double longitude,
        Boolean atende24h,
        double distanciaKm,
        List<TipoSoro> sorosCompativeis
) {
}
