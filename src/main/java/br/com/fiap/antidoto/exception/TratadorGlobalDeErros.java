package br.com.fiap.antidoto.exception;

import br.com.fiap.antidoto.dto.erro.CampoErroResponse;
import br.com.fiap.antidoto.dto.erro.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Todo erro sai no formato do contrato (MVP-01 §7), com mensagens em português
 * prontas para a tela. O app usa o "campos" para pôr cada mensagem embaixo do
 * campo certo; o resto vira a mensagem geral.
 */
@RestControllerAdvice
public class TratadorGlobalDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorGlobalDeErros.class);

    private static final String CAMPOS_INVALIDOS = "Um ou mais campos são inválidos.";

    private final Clock relogio;

    public TratadorGlobalDeErros(Clock relogio) {
        this.relogio = relogio;
    }

    /** Título curto de cada status (o "erro" da resposta). */
    public static String tituloDoStatus(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Dados inválidos";
            case UNAUTHORIZED -> "Não autenticado";
            case FORBIDDEN -> "Acesso negado";
            case NOT_FOUND -> "Não encontrado";
            case METHOD_NOT_ALLOWED -> "Método não permitido";
            case CONFLICT -> "Conflito";
            case UNSUPPORTED_MEDIA_TYPE -> "Formato não suportado";
            default -> "Erro interno";
        };
    }

    /** @Valid no corpo (Bean Validation dos DTOs). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(
            MethodArgumentNotValidException ex, HttpServletRequest requisicao) {
        List<CampoErroResponse> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoErroResponse(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, CAMPOS_INVALIDOS, campos, requisicao);
    }

    /** Validação dos parâmetros da URL (ex.: latitude fora do Brasil, tamanho > 50). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(
            HandlerMethodValidationException ex, HttpServletRequest requisicao) {
        List<CampoErroResponse> campos = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> new CampoErroResponse(
                                resultado.getMethodParameter().getParameterName(),
                                erro.getDefaultMessage())))
                .toList();
        return responder(HttpStatus.BAD_REQUEST, CAMPOS_INVALIDOS, campos, requisicao);
    }

    /** Parâmetro com tipo errado (ex.: tipoAnimal=PEIXE, latitude=abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroComTipoErrado(
            MethodArgumentTypeMismatchException ex, HttpServletRequest requisicao) {
        List<CampoErroResponse> campos = List.of(new CampoErroResponse(ex.getName(), "Valor inválido"));
        return responder(HttpStatus.BAD_REQUEST, CAMPOS_INVALIDOS, campos, requisicao);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> parametroFaltando(
            MissingServletRequestParameterException ex, HttpServletRequest requisicao) {
        List<CampoErroResponse> campos = List.of(
                new CampoErroResponse(ex.getParameterName(), "Campo obrigatório"));
        return responder(HttpStatus.BAD_REQUEST, CAMPOS_INVALIDOS, campos, requisicao);
    }

    /** JSON malformado ou valor desconhecido num enum (ex.: "tipoAnimal": "PEIXE"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(
            HttpMessageNotReadableException ex, HttpServletRequest requisicao) {
        String campo = campoDoJson(ex);
        if (campo != null) {
            List<CampoErroResponse> campos = List.of(new CampoErroResponse(campo, "Valor inválido"));
            return responder(HttpStatus.BAD_REQUEST, CAMPOS_INVALIDOS, campos, requisicao);
        }
        return responder(HttpStatus.BAD_REQUEST, "O corpo da requisição não é um JSON válido.", null, requisicao);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(
            RegraDeNegocioException ex, HttpServletRequest requisicao) {
        if (ex.getCampo() == null) {
            return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), null, requisicao);
        }
        List<CampoErroResponse> campos = List.of(new CampoErroResponse(ex.getCampo(), ex.getMessage()));
        return responder(HttpStatus.BAD_REQUEST, CAMPOS_INVALIDOS, campos, requisicao);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(
            RecursoNaoEncontradoException ex, HttpServletRequest requisicao) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), null, requisicao);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException ex, HttpServletRequest requisicao) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), null, requisicao);
    }

    /** Rota que não existe. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(
            NoResourceFoundException ex, HttpServletRequest requisicao) {
        return responder(HttpStatus.NOT_FOUND, "Recurso não encontrado.", null, requisicao);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoPermitido(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest requisicao) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido nesta rota.", null, requisicao);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> formatoNaoSuportado(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest requisicao) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Envie o corpo em JSON (Content-Type: application/json).", null, requisicao);
    }

    /** Qualquer outro erro: 500 com mensagem genérica; o detalhe fica só no log. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInesperado(Exception ex, HttpServletRequest requisicao) {
        log.error("Erro inesperado em {} {}", requisicao.getMethod(), requisicao.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro inesperado. Tente novamente mais tarde.", null, requisicao);
    }

    private ResponseEntity<ErroResponse> responder(
            HttpStatus status, String mensagem, List<CampoErroResponse> campos, HttpServletRequest requisicao) {
        ErroResponse corpo = new ErroResponse(
                status.value(),
                tituloDoStatus(status),
                mensagem,
                campos,
                requisicao.getRequestURI(),
                Instant.now(relogio).truncatedTo(ChronoUnit.SECONDS)
        );
        return ResponseEntity.status(status).body(corpo);
    }

    /** O nome do campo do JSON que o Jackson não conseguiu ler (o último da trilha). */
    private static String campoDoJson(HttpMessageNotReadableException ex) {
        Throwable causa = ex.getCause();
        while (causa != null) {
            if (causa instanceof JacksonException jackson) {
                return jackson.getPath().stream()
                        .map(JacksonException.Reference::getPropertyName)
                        .filter(nome -> nome != null)
                        .reduce((primeiro, segundo) -> segundo)
                        .orElse(null);
            }
            causa = causa.getCause();
        }
        return null;
    }
}
