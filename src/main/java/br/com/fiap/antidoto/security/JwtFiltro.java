package br.com.fiap.antidoto.security;

import br.com.fiap.antidoto.model.Usuario;
import br.com.fiap.antidoto.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lê o "Authorization: Bearer ..." e, se o token vale e o usuário ainda existe,
 * autentica a requisição com o perfil como papel (ROLE_CIDADAO, ROLE_GESTOR).
 * Token inválido não barra aqui: as rotas públicas (ex.: unidades próximas no SOS)
 * continuam funcionando; as protegidas respondem 401.
 * Não é um @Component: só existe dentro da cadeia do Spring Security (SecurityConfig).
 */
public class JwtFiltro extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtFiltro(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        String cabecalho = requisicao.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            jwtService.idDoUsuario(cabecalho.substring(PREFIXO.length()).trim())
                    .flatMap(usuarioRepository::findById)
                    .ifPresent(this::autenticar);
        }
        cadeia.doFilter(requisicao, resposta);
    }

    private void autenticar(Usuario usuario) {
        UsuarioAutenticado principal =
                new UsuarioAutenticado(usuario.getId(), usuario.getEmail(), usuario.getPerfil());
        var autenticacao = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name())));
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }
}
