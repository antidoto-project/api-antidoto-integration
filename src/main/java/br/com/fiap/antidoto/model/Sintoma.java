package br.com.fiap.antidoto.model;

/** Sintomas da triagem (MVP-00 §4.2): o texto da tela e se é sinal de alerta (RN-U1). */
public enum Sintoma {
    DOR_LOCAL("Dor no local", false),
    INCHACO_LOCAL("Inchaço no local", false),
    VERMELHIDAO("Vermelhidão ou mancha roxa", false),
    DORMENCIA("Formigamento ou dormência", false),
    FALTA_DE_AR("Dificuldade para respirar", true),
    VISAO_TURVA("Visão turva ou pálpebras caídas", true),
    VOMITOS("Vômitos repetidos", true),
    SUOR_INTENSO("Suor intenso pelo corpo", true),
    SONOLENCIA_DESMAIO("Sonolência, agitação forte ou desmaio", true),
    SANGRAMENTO("Sangramento ou urina escura", true),
    INCHACO_ROSTO("Inchaço no rosto, lábios ou garganta", true),
    MUITAS_PICADAS_ALERGIA("Muitas picadas (10+) ou alergia conhecida", true);

    private final String texto;
    private final boolean alerta;

    Sintoma(String texto, boolean alerta) {
        this.texto = texto;
        this.alerta = alerta;
    }

    public String getTexto() { return texto; }

    public boolean isAlerta() { return alerta; }
}
