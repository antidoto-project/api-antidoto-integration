package br.com.fiap.antidoto.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * POST /api/auth/cadastro. As mensagens são as mesmas do app. O e-mail chega já
 * aparado e em minúsculas, e o telefone vazio vira null (opcional).
 */
public record CadastroRequest(

        @NotBlank(message = "Informe o nome")
        @Size(min = 2, max = 120, message = "Informe o nome (pelo menos 2 letras)")
        String nome,

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 160, message = "O e-mail pode ter até 160 caracteres")
        String email,

        @NotBlank(message = "Informe a senha")
        @Size(min = 8, max = 72, message = "A senha precisa ter de 8 a 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha precisa ter pelo menos uma letra e um número")
        String senha,

        @Pattern(regexp = "^[0-9]{10,11}$", message = "Telefone inválido: use o DDD e o número")
        String telefone

) {

    public CadastroRequest {
        nome = nome == null ? null : nome.trim();
        email = email == null ? null : email.trim().toLowerCase();
        telefone = telefone == null || telefone.isBlank() ? null : telefone.trim();
    }
}
