package br.com.fiap.antidoto.exception;

/** 404: não existe ou é de outro usuário (MVP-01 §5: nunca 403 para recurso alheio). */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
