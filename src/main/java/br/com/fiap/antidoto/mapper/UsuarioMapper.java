package br.com.fiap.antidoto.mapper;

import br.com.fiap.antidoto.dto.usuario.UsuarioResponse;
import br.com.fiap.antidoto.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponse paraResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getPerfil(),
                usuario.getCriadoEm()
        );
    }
}
