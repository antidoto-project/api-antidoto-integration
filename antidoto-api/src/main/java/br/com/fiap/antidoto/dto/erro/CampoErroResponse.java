package br.com.fiap.antidoto.dto.erro;

/** Um campo inválido: o nome é o do JSON, para o app mostrar embaixo do campo certo. */
public record CampoErroResponse(
        String campo,
        String mensagem
) {
}
