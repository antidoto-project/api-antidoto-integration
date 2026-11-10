package br.com.fiap.antidoto.dto.usuario;

import jakarta.validation.constraints.NotBlank;

/** POST /api/auth/login. O e-mail chega já aparado e em minúsculas, como no cadastro. */
public record LoginRequest(

        @NotBlank(message = "Informe o e-mail")
        String email,

        @NotBlank(message = "Informe a senha")
        String senha

) {

    public LoginRequest {
        email = email == null ? null : email.trim().toLowerCase();
    }
}
