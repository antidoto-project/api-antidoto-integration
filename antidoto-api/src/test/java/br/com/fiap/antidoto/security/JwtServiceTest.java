package br.com.fiap.antidoto.security;

import br.com.fiap.antidoto.model.Perfil;
import br.com.fiap.antidoto.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SEGREDO = Base64.getEncoder().encodeToString(new byte[32]);
    private static final String OUTRO_SEGREDO = Base64.getEncoder().encodeToString("x".repeat(32).getBytes());
    private static final Instant AGORA = Instant.parse("2026-11-10T22:15:00Z");
    private static final long UM_DIA = 86_400;

    private static JwtService servico(String segredo, Instant instante) {
        return new JwtService(segredo, UM_DIA, Clock.fixed(instante, ZoneOffset.UTC));
    }

    private static Usuario ana() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail("ana@teste.com");
        usuario.setPerfil(Perfil.CIDADAO);
        return usuario;
    }

    @Test
    void oTokenGeradoDevolveOIdDoUsuario() {
        JwtService jwt = servico(SEGREDO, AGORA);

        String token = jwt.gerarToken(ana());

        assertThat(jwt.idDoUsuario(token)).contains(7L);
        assertThat(jwt.getValidadeEmSegundos()).isEqualTo(UM_DIA);
    }

    @Test
    void depoisDe24HorasOTokenNaoVale() {
        String token = servico(SEGREDO, AGORA).gerarToken(ana());

        assertThat(servico(SEGREDO, AGORA.plus(Duration.ofHours(23))).idDoUsuario(token)).contains(7L);
        assertThat(servico(SEGREDO, AGORA.plus(Duration.ofHours(25))).idDoUsuario(token)).isEmpty();
    }

    @Test
    void tokenAssinadoComOutroSegredoOuMalformadoNaoVale() {
        String token = servico(OUTRO_SEGREDO, AGORA).gerarToken(ana());

        assertThat(servico(SEGREDO, AGORA).idDoUsuario(token)).isEmpty();
        assertThat(servico(SEGREDO, AGORA).idDoUsuario("abc")).isEmpty();
        assertThat(servico(SEGREDO, AGORA).idDoUsuario("")).isEmpty();
    }

    @Test
    void semJwtSecretUsaUmSegredoAleatorioPorSubida() {
        JwtService primeiraSubida = servico("", AGORA);
        JwtService segundaSubida = servico("", AGORA);
        String token = primeiraSubida.gerarToken(ana());

        assertThat(primeiraSubida.idDoUsuario(token)).contains(7L);
        assertThat(segundaSubida.idDoUsuario(token)).isEmpty();
    }

    @Test
    void jwtSecretCurtoOuForaDoBase64ParaASubida() {
        assertThatThrownBy(() -> servico("c2hvcnQ=", AGORA))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pelo menos 32 bytes");
        assertThatThrownBy(() -> servico("isto não é base64!", AGORA))
                .isInstanceOf(IllegalStateException.class);
    }
}
