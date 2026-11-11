package br.com.fiap.antidoto.service;

import br.com.fiap.antidoto.dto.usuario.AtualizarUsuarioRequest;
import br.com.fiap.antidoto.dto.usuario.UsuarioResponse;
import br.com.fiap.antidoto.exception.RecursoNaoEncontradoException;
import br.com.fiap.antidoto.mapper.UsuarioMapper;
import br.com.fiap.antidoto.model.Usuario;
import br.com.fiap.antidoto.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Dados de quem está logado (GET e PUT /api/usuarios/me). */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long idUsuario) {
        return usuarioMapper.paraResponse(carregar(idUsuario));
    }

    /** Só nome e telefone (MVP-01 §6.1); telefone null apaga. */
    @Transactional
    public UsuarioResponse atualizar(Long idUsuario, AtualizarUsuarioRequest requisicao) {
        Usuario usuario = carregar(idUsuario);
        usuario.setNome(requisicao.nome());
        usuario.setTelefone(requisicao.telefone());
        return usuarioMapper.paraResponse(usuario);
    }

    private Usuario carregar(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }
}
