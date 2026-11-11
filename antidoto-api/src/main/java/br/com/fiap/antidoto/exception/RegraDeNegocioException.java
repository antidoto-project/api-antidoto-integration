package br.com.fiap.antidoto.exception;

/**
 * 400 de uma regra que o Bean Validation não cobre (ex.: RN-G4, hora da picada).
 * Com campo, a mensagem vai para o campo certo na tela do app.
 */
public class RegraDeNegocioException extends RuntimeException {

    private final String campo;

    public RegraDeNegocioException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
