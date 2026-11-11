package br.com.fiap.antidoto.security;

import br.com.fiap.antidoto.dto.erro.ErroResponse;
import br.com.fiap.antidoto.exception.TratadorGlobalDeErros;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 401 e 403 do Spring Security no mesmo formato de erro do contrato (MVP-01 §5 e §7).
 * Eles acontecem antes do controller, então o TratadorGlobalDeErros não os vê.
 */
@Component
public class RespostasDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;
    private final Clock relogio;

    public RespostasDeSeguranca(JsonMapper jsonMapper, Clock relogio) {
        this.jsonMapper = jsonMapper;
        this.relogio = relogio;
    }

    /** Sem login (ou com token vencido/inválido) numa rota protegida. */
    @Override
    public void commence(
            HttpServletRequest requisicao, HttpServletResponse resposta, AuthenticationException ex)
            throws IOException {
        boolean mandouToken = requisicao.getHeader(HttpHeaders.AUTHORIZATION) != null;
        String mensagem = mandouToken
                ? "Sessão expirada ou inválida. Entre novamente."
                : "Faça login para continuar.";
        escrever(requisicao, resposta, HttpStatus.UNAUTHORIZED, mensagem);
    }

    /** Logado, mas o perfil não pode (ex.: CIDADAO numa rota de GESTOR). */
    @Override
    public void handle(
            HttpServletRequest requisicao, HttpServletResponse resposta, AccessDeniedException ex)
            throws IOException {
        escrever(requisicao, resposta, HttpStatus.FORBIDDEN, "Você não tem permissão para esta ação.");
    }

    private void escrever(
            HttpServletRequest requisicao, HttpServletResponse resposta, HttpStatus status, String mensagem)
            throws IOException {
        ErroResponse erro = new ErroResponse(
                status.value(),
                TratadorGlobalDeErros.tituloDoStatus(status),
                mensagem,
                null,
                requisicao.getRequestURI(),
                Instant.now(relogio).truncatedTo(ChronoUnit.SECONDS)
        );
        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(resposta.getOutputStream(), erro);
    }
}
