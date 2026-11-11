package br.com.fiap.antidoto.ia;

import br.com.fiap.antidoto.model.FaixaEtaria;
import br.com.fiap.antidoto.model.Ocorrencia;
import br.com.fiap.antidoto.model.Sintoma;
import br.com.fiap.antidoto.model.TipoAnimal;
import br.com.fiap.antidoto.model.UnidadeSaude;
import br.com.fiap.antidoto.model.Urgencia;
import br.com.fiap.antidoto.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class ResumoModeloPadraoTest {

    /** 22:15 em Brasília (01:15 UTC do dia seguinte). */
    private static final Instant PICADA = Instant.parse("2026-11-11T01:15:00Z");

    private final ResumoModeloPadrao modelo = new ResumoModeloPadrao();

    private static Ocorrencia escorpiaoEmCrianca() {
        Usuario ana = new Usuario();
        ana.setNome("Ana Souza");
        ana.setEmail("ana@teste.com");
        ana.setTelefone("11999990000");

        UnidadeSaude unidade = new UnidadeSaude();
        unidade.setNome("Hospital Exemplo Centro");

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setUsuario(ana);
        ocorrencia.setUnidade(unidade);
        ocorrencia.setTipoAnimal(TipoAnimal.ESCORPIAO);
        ocorrencia.setFaixaEtaria(FaixaEtaria.CRIANCA);
        ocorrencia.setSintomas(EnumSet.of(Sintoma.VOMITOS, Sintoma.DOR_LOCAL));
        ocorrencia.setHoraPicada(PICADA);
        ocorrencia.setLatitude(-23.5505);
        ocorrencia.setLongitude(-46.6333);
        ocorrencia.setUrgencia(Urgencia.EMERGENCIA);
        ocorrencia.setMotivos("Sinal de alerta;Escorpião em criança");
        return ocorrencia;
    }

    @Test
    void montaOResumoDoMvp01ComOsDadosDaTriagem() {
        String resumo = modelo.montar(escorpiaoEmCrianca(), PICADA.plus(Duration.ofMinutes(25)));

        assertThat(resumo).isEqualTo(String.join("\n",
                "PRÉ-ATENDIMENTO — Antídoto",
                "Animal: Escorpião · Faixa etária: criança",
                "Picada: 22:15 (há 25 min)",
                "Sintomas: Dor no local; Vômitos repetidos (ALERTA)",
                "Urgência: EMERGÊNCIA — Sinal de alerta; Escorpião em criança",
                "Destino: Hospital Exemplo Centro"));
    }

    @Test
    void nuncaLevaNomeEmailTelefoneOuCoordenadas() {
        String resumo = modelo.montar(escorpiaoEmCrianca(), PICADA.plus(Duration.ofMinutes(25)));

        // RN-G6
        assertThat(resumo).doesNotContain("Ana", "ana@teste.com", "11999990000", "-23.5505", "-46.6333");
    }

    @Test
    void semSintomasSemUnidadeEDeOutroDia() {
        Ocorrencia ocorrencia = escorpiaoEmCrianca();
        ocorrencia.setSintomas(EnumSet.noneOf(Sintoma.class));
        ocorrencia.setUnidade(null);

        String resumo = modelo.montar(ocorrencia, PICADA.plus(Duration.ofHours(26).plusMinutes(10)));

        assertThat(resumo)
                .contains("Picada: 10/11 22:15 (há 26 h 10 min)")
                .contains("Sintomas: nenhum informado")
                .contains("Destino: não escolhido");
    }

    @Test
    void tempoDecorrido() {
        assertThat(ResumoModeloPadrao.tempoDecorrido(Duration.ofMinutes(5))).isEqualTo("há 5 min");
        assertThat(ResumoModeloPadrao.tempoDecorrido(Duration.ofHours(3))).isEqualTo("há 3 h");
        assertThat(ResumoModeloPadrao.tempoDecorrido(Duration.ofMinutes(250))).isEqualTo("há 4 h 10 min");
        assertThat(ResumoModeloPadrao.tempoDecorrido(Duration.ofMinutes(-2))).isEqualTo("há 0 min");
    }
}
