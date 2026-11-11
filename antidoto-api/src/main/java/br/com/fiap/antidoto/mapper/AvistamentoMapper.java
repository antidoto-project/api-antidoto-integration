package br.com.fiap.antidoto.mapper;

import br.com.fiap.antidoto.dto.avistamento.AvistamentoResponse;
import br.com.fiap.antidoto.model.Avistamento;
import org.springframework.stereotype.Component;

@Component
public class AvistamentoMapper {

    public AvistamentoResponse paraResponse(Avistamento avistamento) {
        return new AvistamentoResponse(
                avistamento.getId(),
                avistamento.getTipoAnimal(),
                avistamento.getLocal(),
                avistamento.getLatitude(),
                avistamento.getLongitude(),
                avistamento.getDataHora(),
                avistamento.getObservacao(),
                avistamento.getCriadoEm(),
                avistamento.getAtualizadoEm()
        );
    }
}
