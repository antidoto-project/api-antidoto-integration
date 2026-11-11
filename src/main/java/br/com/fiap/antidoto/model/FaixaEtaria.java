package br.com.fiap.antidoto.model;

/** Faixa etária de quem foi picado: CRIANCA (0–9), ADULTO (10–59), IDOSO (60+). */
public enum FaixaEtaria {
    CRIANCA("criança"),
    ADULTO("adulto"),
    IDOSO("idoso");

    private final String rotulo;

    FaixaEtaria(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() { return rotulo; }
}
