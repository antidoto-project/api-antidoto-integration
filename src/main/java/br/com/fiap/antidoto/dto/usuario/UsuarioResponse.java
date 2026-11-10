package br.com.fiap.antidoto.dto.usuario;

import br.com.fiap.antidoto.model.Perfil;

import java.time.Instant;

/** Schema Usuario do contrato (nunca a senha). */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        Perfil perfil,
        Instant criadoEm
) {
}
