package br.com.fiap.antidoto.security;

import br.com.fiap.antidoto.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Gera e confere o JWT (MVP-01 §5): HS256, validade de 24 h, sem refresh token.
 * Claims: sub = id do usuário, email e perfil.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey chave;
    private final long validadeEmSegundos;
    private final Clock relogio;

    public JwtService(
            @Value("${antidoto.jwt.segredo}") String segredo,
            @Value("${antidoto.jwt.validade-em-segundos}") long validadeEmSegundos,
            Clock relogio
    ) {
        this.chave = criarChave(segredo);
        this.validadeEmSegundos = validadeEmSegundos;
        this.relogio = relogio;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now(relogio);
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .claim("perfil", usuario.getPerfil().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(validadeEmSegundos)))
                .signWith(chave)
                .compact();
    }

    public long getValidadeEmSegundos() {
        return validadeEmSegundos;
    }

    /** O id do dono de um token válido (assinatura e validade conferidas); vazio se não vale. */
    public Optional<Long> idDoUsuario(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .clock(() -> Date.from(relogio.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            // Token vencido, adulterado ou malformado: a requisição segue sem usuário
            return Optional.empty();
        }
    }

    /**
     * JWT_SECRET em base64, com pelo menos 32 bytes (ex.: openssl rand -base64 32). Sem ele,
     * um segredo aleatório: os tokens deixam de valer quando a API reinicia.
     */
    private static SecretKey criarChave(String segredo) {
        if (segredo == null || segredo.isBlank()) {
            log.warn("JWT_SECRET não definido: usando um segredo aleatório. Os logins perdem a validade quando a API reinicia.");
            byte[] aleatorio = new byte[32];
            new SecureRandom().nextBytes(aleatorio);
            return Keys.hmacShaKeyFor(aleatorio);
        }
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(segredo.trim()));
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "JWT_SECRET precisa estar em base64 e ter pelo menos 32 bytes (ex.: openssl rand -base64 32)", e);
        }
    }
}
