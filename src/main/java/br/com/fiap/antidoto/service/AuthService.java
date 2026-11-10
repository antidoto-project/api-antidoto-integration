package br.com.fiap.antidoto.service;

import br.com.fiap.antidoto.dto.usuario.AuthResponse;
import br.com.fiap.antidoto.dto.usuario.CadastroRequest;
import br.com.fiap.antidoto.dto.usuario.LoginRequest;
import br.com.fiap.antidoto.exception.ConflitoException;
import br.com.fiap.antidoto.exception.CredenciaisInvalidasException;
import br.com.fiap.antidoto.mapper.UsuarioMapper;
import br.com.fiap.antidoto.model.Perfil;
import br.com.fiap.antidoto.model.Usuario;
import br.com.fiap.antidoto.repository.UsuarioRepository;
import br.com.fiap.antidoto.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Cadastro e login (MVP-01 §6.1). Os dois já devolvem o token: o app entra direto. */
@Service
public class AuthService {

    private static final String TIPO_DO_TOKEN = "Bearer";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorDeSenha;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;
    private final Clock relogio;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder codificadorDeSenha,
            JwtService jwtService,
            UsuarioMapper usuarioMapper,
            Clock relogio
    ) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorDeSenha = codificadorDeSenha;
        this.jwtService = jwtService;
        this.usuarioMapper = usuarioMapper;
        this.relogio = relogio;
    }

    /** Cria um CIDADAO (o GESTOR só existe pelos dados iniciais). E-mail repetido: 409. */
    @Transactional
    public AuthResponse cadastrar(CadastroRequest requisicao) {
        if (usuarioRepository.existsByEmail(requisicao.email())) {
            throw new ConflitoException("E-mail já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(requisicao.nome());
        usuario.setEmail(requisicao.email());
        usuario.setSenhaHash(codificadorDeSenha.encode(requisicao.senha()));
        usuario.setTelefone(requisicao.telefone());
        usuario.setPerfil(Perfil.CIDADAO);
        usuario.setCriadoEm(Instant.now(relogio).truncatedTo(ChronoUnit.MILLIS));

        return autenticacaoDe(usuarioRepository.save(usuario));
    }

    /** E-mail que não existe e senha errada dão a mesma resposta (não revela quem tem conta). */
    @Transactional(readOnly = true)
    public AuthResponse entrar(LoginRequest requisicao) {
        Usuario usuario = usuarioRepository.findByEmail(requisicao.email())
                .filter(encontrado -> codificadorDeSenha.matches(requisicao.senha(), encontrado.getSenhaHash()))
                .orElseThrow(CredenciaisInvalidasException::new);

        return autenticacaoDe(usuario);
    }

    private AuthResponse autenticacaoDe(Usuario usuario) {
        return new AuthResponse(
                jwtService.gerarToken(usuario),
                TIPO_DO_TOKEN,
                jwtService.getValidadeEmSegundos(),
                usuarioMapper.paraResponse(usuario)
        );
    }
}
