package br.com.fiap.antidoto.service;

import br.com.fiap.antidoto.dto.ocorrencia.ResultadoUrgencia;
import br.com.fiap.antidoto.model.FaixaEtaria;
import br.com.fiap.antidoto.model.Sintoma;
import br.com.fiap.antidoto.model.TipoAnimal;
import br.com.fiap.antidoto.model.Urgencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** RN-U (MVP-00 §4.3): os MESMOS 10 casos do urgencia.test.ts do app. Não mude sem combinar. */
class UrgenciaServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-09T21:00:00Z");

    private final UrgenciaService urgencia = new UrgenciaService();

    private static List<Sintoma> sintomas(String lista) {
        return lista == null || lista.isBlank()
                ? List.of()
                : Arrays.stream(lista.split(" ")).map(Sintoma::valueOf).toList();
    }

    @ParameterizedTest(name = "caso {0}: {1}, {2}, {3}, há {4} min → {5}")
    @CsvSource({
            "1,  ESCORPIAO, ADULTO,  DOR_LOCAL,              10,  MODERADA",
            "2,  ESCORPIAO, CRIANCA, '',                     10,  EMERGENCIA",
            "3,  ESCORPIAO, IDOSO,   '',                     10,  ALTA",
            "4,  SERPENTE,  ADULTO,  '',                     10,  ALTA",
            "5,  ARANHA,    ADULTO,  FALTA_DE_AR,            10,  EMERGENCIA",
            "6,  NAO_SEI,   ADULTO,  '',                     10,  ALTA",
            "7,  ESCORPIAO, ADULTO,  '',                     240, ALTA",
            "8,  SERPENTE,  ADULTO,  '',                     240, EMERGENCIA",
            "9,  ABELHA,    ADULTO,  MUITAS_PICADAS_ALERGIA, 10,  EMERGENCIA",
            "10, ABELHA,    ADULTO,  DOR_LOCAL,              10,  MODERADA",
    })
    void casosObrigatorios(int caso, TipoAnimal animal, FaixaEtaria faixa, String lista, long minutos,
                           Urgencia esperada) {
        ResultadoUrgencia resultado = urgencia.calcular(
                animal, faixa, sintomas(lista), AGORA.minus(Duration.ofMinutes(minutos)), AGORA);

        assertThat(resultado.urgencia()).isEqualTo(esperada);
        // ligar192 só em EMERGENCIA (RN-G3)
        assertThat(resultado.ligar192()).isEqualTo(esperada == Urgencia.EMERGENCIA);
    }

    @Test
    void semNenhumaRegraOMotivoEAvaliacaoNecessaria() {
        ResultadoUrgencia resultado = urgencia.calcular(TipoAnimal.ESCORPIAO, FaixaEtaria.ADULTO,
                List.of(Sintoma.DOR_LOCAL), AGORA.minus(Duration.ofMinutes(1)), AGORA);

        assertThat(resultado.urgencia()).isEqualTo(Urgencia.MODERADA);
        assertThat(resultado.motivos()).containsExactly("Avaliação necessária");
    }

    @Test
    void juntaOsMotivosNaOrdemDasRegrasSemRepetir() {
        ResultadoUrgencia resultado = urgencia.calcular(TipoAnimal.ESCORPIAO, FaixaEtaria.CRIANCA,
                List.of(Sintoma.FALTA_DE_AR, Sintoma.VOMITOS), AGORA.minus(Duration.ofMinutes(1)), AGORA);

        assertThat(resultado.motivos()).containsExactly("Sinal de alerta", "Escorpião em criança", "Criança ou idoso");
    }

    @Test
    void com3HorasExatasSobeUmNivelECom2h59NaoSobe() {
        ResultadoUrgencia tresHoras = urgencia.calcular(TipoAnimal.ABELHA, FaixaEtaria.ADULTO, List.of(),
                AGORA.minus(Duration.ofHours(3)), AGORA);
        ResultadoUrgencia quaseTres = urgencia.calcular(TipoAnimal.ABELHA, FaixaEtaria.ADULTO, List.of(),
                AGORA.minus(Duration.ofHours(3).minusMinutes(1)), AGORA);

        assertThat(tresHoras.urgencia()).isEqualTo(Urgencia.ALTA);
        assertThat(tresHoras.motivos()).containsExactly("Mais de 3 horas desde a picada");
        assertThat(quaseTres.urgencia()).isEqualTo(Urgencia.MODERADA);
    }

    @Test
    void emergenciaNaoSobeNemGanhaOMotivoDas3Horas() {
        ResultadoUrgencia resultado = urgencia.calcular(TipoAnimal.ESCORPIAO, FaixaEtaria.CRIANCA, List.of(),
                AGORA.minus(Duration.ofHours(5)), AGORA);

        assertThat(resultado.urgencia()).isEqualTo(Urgencia.EMERGENCIA);
        assertThat(resultado.motivos()).doesNotContain("Mais de 3 horas desde a picada");
    }
}
