package br.com.fiap.antidoto.model;

/**
 * Nível de urgência (MVP-00 §4.3). A ordem da declaração é a da regra:
 * MODERADA < ALTA < EMERGENCIA. Não existe "baixa" nem "sem urgência".
 */
public enum Urgencia {
    MODERADA("MODERADA"),
    ALTA("ALTA"),
    EMERGENCIA("EMERGÊNCIA");

    private final String rotulo;

    Urgencia(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() { return rotulo; }

    /** O maior entre os dois níveis (RN-U: nivel = max(nivel, ...)). */
    public Urgencia maior(Urgencia outra) {
        return compareTo(outra) >= 0 ? this : outra;
    }

    /** Um nível acima (RN-U5); EMERGENCIA continua EMERGENCIA. */
    public Urgencia proxima() {
        return this == EMERGENCIA ? EMERGENCIA : values()[ordinal() + 1];
    }
}
