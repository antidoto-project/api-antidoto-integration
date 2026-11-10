package br.com.fiap.antidoto.mapper;

import br.com.fiap.antidoto.dto.unidade.UnidadeProximaResponse;
import br.com.fiap.antidoto.dto.unidade.UnidadeResponse;
import br.com.fiap.antidoto.model.TipoSoro;
import br.com.fiap.antidoto.model.UnidadeSaude;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UnidadeMapper {

    public UnidadeResponse paraResponse(UnidadeSaude unidade) {
        return new UnidadeResponse(
                unidade.getId(),
                unidade.getNome(),
                unidade.getTipo(),
                unidade.getEndereco(),
                unidade.getTelefone(),
                unidade.getLatitude(),
                unidade.getLongitude(),
                unidade.getAtende24h(),
                unidade.getSoros().stream().sorted().toList(),
                unidade.getAtiva(),
                unidade.getAtualizadoEm()
        );
    }

    public UnidadeProximaResponse paraProxima(
            UnidadeSaude unidade, double distanciaKm, List<TipoSoro> sorosCompativeis) {
        return new UnidadeProximaResponse(
                unidade.getId(),
                unidade.getNome(),
                unidade.getTipo(),
                unidade.getEndereco(),
                unidade.getTelefone(),
                unidade.getLatitude(),
                unidade.getLongitude(),
                unidade.getAtende24h(),
                distanciaKm,
                sorosCompativeis
        );
    }
}
