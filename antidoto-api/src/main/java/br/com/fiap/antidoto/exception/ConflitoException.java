package br.com.fiap.antidoto.exception;

/** 409 (ex.: e-mail já cadastrado). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
