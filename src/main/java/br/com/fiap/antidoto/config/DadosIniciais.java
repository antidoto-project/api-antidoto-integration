package br.com.fiap.antidoto.config;

import br.com.fiap.antidoto.model.Perfil;
import br.com.fiap.antidoto.model.Usuario;
import br.com.fiap.antidoto.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Dados de teste (MVP-01 §10), criados na subida só se ainda não existirem.
 * Os mesmos usuários do Java do grupo, para o app e o vídeo funcionarem com os dois.
 * Desligue com CARREGAR_DADOS_INICIAIS=false.
 */
@Component
public class DadosIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    /** Senha dos usuários de teste: pública no README (dados fictícios). */
    static final String SENHA_DE_TESTE = "Teste@123";

    private final boolean carregar;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorDeSenha;
    private final Clock relogio;

    public DadosIniciais(
            @Value("${antidoto.dados-iniciais}") boolean carregar,
            UsuarioRepository usuarioRepository,
            PasswordEncoder codificadorDeSenha,
            Clock relogio
    ) {
        this.carregar = carregar;
        this.usuarioRepository = usuarioRepository;
        this.codificadorDeSenha = codificadorDeSenha;
        this.relogio = relogio;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (!carregar) {
            return;
        }
        criarUsuarioSeNaoExistir("cidadao@sospeconha.dev", "Ana Souza", "11999990000", Perfil.CIDADAO);
        criarUsuarioSeNaoExistir("gestor@sospeconha.dev", "Gestor de Teste", null, Perfil.GESTOR);
        log.info("Dados iniciais conferidos (usuários de teste)");
    }

    private void criarUsuarioSeNaoExistir(String email, String nome, String telefone, Perfil perfil) {
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(codificadorDeSenha.encode(SENHA_DE_TESTE));
        usuario.setTelefone(telefone);
        usuario.setPerfil(perfil);
        usuario.setCriadoEm(Instant.now(relogio).truncatedTo(ChronoUnit.MILLIS));
        usuarioRepository.save(usuario);
    }
}
