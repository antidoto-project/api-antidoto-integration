package br.com.fiap.antidoto.model;

/** Animal da picada ou do avistamento (MVP-00 §4.1). O rótulo é o mesmo do app. */
public enum TipoAnimal {
    ESCORPIAO("Escorpião"),
    SERPENTE("Cobra"),
    ARANHA("Aranha"),
    LAGARTA("Lagarta/Taturana"),
    ABELHA("Abelha/Vespa"),
    NAO_SEI("Não sei");

    private final String rotulo;

    TipoAnimal(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() { return rotulo; }
}
