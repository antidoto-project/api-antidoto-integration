package br.com.fiap.antidoto.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PUT /api/usuarios/me: só nome e telefone (o e-mail não muda). Telefone vazio apaga. */
public record AtualizarUsuarioRequest(

        @NotBlank(message = "Informe o nome")
        @Size(min = 2, max = 120, message = "Informe o nome (pelo menos 2 letras)")
        String nome,

        @Pattern(regexp = "^[0-9]{10,11}$", message = "Telefone inválido: use o DDD e o número")
        String telefone

) {

    public AtualizarUsuarioRequest {
        nome = nome == null ? null : nome.trim();
        telefone = telefone == null || telefone.isBlank() ? null : telefone.trim();
    }
}
