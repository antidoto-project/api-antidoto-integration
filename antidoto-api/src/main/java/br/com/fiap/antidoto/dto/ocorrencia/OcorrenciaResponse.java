package br.com.fiap.antidoto.dto.ocorrencia;

import br.com.fiap.antidoto.model.FaixaEtaria;
import br.com.fiap.antidoto.model.Sintoma;
import br.com.fiap.antidoto.model.StatusResumo;
import br.com.fiap.antidoto.model.TipoAnimal;
import br.com.fiap.antidoto.model.Urgencia;

import java.time.Instant;
import java.util.List;

/**
 * Schema Ocorrencia do contrato. "resumo" fica null enquanto o status é PENDENTE;
 * o app consulta GET /api/ocorrencias/{id} a cada 3 s até mudar.
 */
public record OcorrenciaResponse(
        Long id,
        TipoAnimal tipoAnimal,
        FaixaEtaria faixaEtaria,
        List<Sintoma> sintomas,
        Instant horaPicada,
        Double latitude,
        Double longitude,
        Urgencia urgencia,
        List<String> motivos,
        boolean ligar192,
        Long unidadeId,
        String unidadeNome,
        StatusResumo statusResumo,
        String resumo,
        String avisoResumo,
        String aviso,
        Instant criadoEm
) {
}
