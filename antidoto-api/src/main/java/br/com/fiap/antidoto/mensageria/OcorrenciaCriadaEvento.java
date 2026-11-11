package br.com.fiap.antidoto.mensageria;

/** Uma ocorrência acabou de ser registrada e precisa do resumo de pré-atendimento. */
public record OcorrenciaCriadaEvento(
        Long ocorrenciaId
) {
}
