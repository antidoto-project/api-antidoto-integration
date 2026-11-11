package br.com.fiap.antidoto.security;

import br.com.fiap.antidoto.model.Perfil;

/** Quem fez a requisição, lido do token e conferido no banco (o principal do Spring Security). */
public record UsuarioAutenticado(
        Long id,
        String email,
        Perfil perfil
) {
}
