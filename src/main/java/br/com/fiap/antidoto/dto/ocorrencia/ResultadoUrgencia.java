package br.com.fiap.antidoto.dto.ocorrencia;

import br.com.fiap.antidoto.model.Urgencia;

import java.util.List;

/** O que a RN-U decide: o nível, os motivos (na ordem das regras) e se é para ligar 192 (RN-G3). */
public record ResultadoUrgencia(
        Urgencia urgencia,
        List<String> motivos,
        boolean ligar192
) {
}
