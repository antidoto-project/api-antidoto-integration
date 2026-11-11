package br.com.fiap.antidoto.exception;

/** Login com e-mail ou senha errados: 401 com mensagem genérica (MVP-01 §5). */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha incorretos");
    }
}
