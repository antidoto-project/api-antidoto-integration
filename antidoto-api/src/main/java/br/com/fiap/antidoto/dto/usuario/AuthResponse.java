package br.com.fiap.antidoto.dto.usuario;

/** Resposta do cadastro e do login: o app guarda o token e o usuário. */
public record AuthResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        UsuarioResponse usuario
) {
}
