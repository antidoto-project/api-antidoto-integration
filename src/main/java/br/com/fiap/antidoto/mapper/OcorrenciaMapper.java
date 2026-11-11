package br.com.fiap.antidoto.mapper;

import br.com.fiap.antidoto.dto.ocorrencia.OcorrenciaResponse;
import br.com.fiap.antidoto.model.Ocorrencia;
import br.com.fiap.antidoto.model.UnidadeSaude;
import br.com.fiap.antidoto.model.Urgencia;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OcorrenciaMapper {

    /** RN-G7: o texto que acompanha o resumo. */
    public static final String AVISO_RESUMO = "Resumo gerado por IA. Não substitui avaliação profissional.";

    /** RN-G2: o texto fixo de toda tela de resultado. */
    public static final String AVISO_ATENDIMENTO = "Procure atendimento médico imediatamente.";

    /** Separador dos motivos na coluna "motivos" (MVP-01 §4.1). */
    public static final String SEPARADOR_DE_MOTIVOS = ";";

    public OcorrenciaResponse paraResponse(Ocorrencia ocorrencia) {
        UnidadeSaude unidade = ocorrencia.getUnidade();
        return new OcorrenciaResponse(
                ocorrencia.getId(),
                ocorrencia.getTipoAnimal(),
                ocorrencia.getFaixaEtaria(),
                ocorrencia.getSintomas().stream().sorted().toList(),
                ocorrencia.getHoraPicada(),
                ocorrencia.getLatitude(),
                ocorrencia.getLongitude(),
                ocorrencia.getUrgencia(),
                motivosDaColuna(ocorrencia.getMotivos()),
                ocorrencia.getUrgencia() == Urgencia.EMERGENCIA,
                unidade == null ? null : unidade.getId(),
                unidade == null ? null : unidade.getNome(),
                ocorrencia.getStatusResumo(),
                ocorrencia.getResumo(),
                AVISO_RESUMO,
                AVISO_ATENDIMENTO,
                ocorrencia.getCriadoEm()
        );
    }

    public static String motivosParaColuna(List<String> motivos) {
        return String.join(SEPARADOR_DE_MOTIVOS, motivos);
    }

    public static List<String> motivosDaColuna(String motivos) {
        if (motivos == null || motivos.isBlank()) {
            return List.of();
        }
        return Arrays.stream(motivos.split(SEPARADOR_DE_MOTIVOS)).map(String::trim).toList();
    }
}
