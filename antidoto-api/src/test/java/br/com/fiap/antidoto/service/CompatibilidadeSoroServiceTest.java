package br.com.fiap.antidoto.service;

import br.com.fiap.antidoto.model.TipoAnimal;
import br.com.fiap.antidoto.model.TipoSoro;
import br.com.fiap.antidoto.model.TipoUnidade;
import br.com.fiap.antidoto.model.UnidadeSaude;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static br.com.fiap.antidoto.model.TipoSoro.*;
import static org.assertj.core.api.Assertions.assertThat;

/** RN-S (MVP-00 §4.4): cada linha da tabela, inclusive NAO_SEI e ABELHA. */
class CompatibilidadeSoroServiceTest {

    private final CompatibilidadeSoroService servico = new CompatibilidadeSoroService();

    private static UnidadeSaude unidade(TipoUnidade tipo, TipoSoro... soros) {
        UnidadeSaude unidade = new UnidadeSaude();
        unidade.setTipo(tipo);
        Set<TipoSoro> conjunto = EnumSet.noneOf(TipoSoro.class);
        conjunto.addAll(Arrays.asList(soros));
        unidade.setSoros(conjunto);
        return unidade;
    }

    @ParameterizedTest(name = "{0} com {1} → {2}")
    @CsvSource({
            "ESCORPIAO, ANTIESCORPIONICO, true",
            "ESCORPIAO, ANTIARACNIDICO, true",
            "ESCORPIAO, ANTIBOTROPICO, false",
            "SERPENTE, ANTIBOTROPICO, true",
            "SERPENTE, ANTICROTALICO, true",
            "SERPENTE, ANTIELAPIDICO, true",
            "SERPENTE, ANTILAQUETICO, true",
            "SERPENTE, ANTIESCORPIONICO, false",
            "ARANHA, ANTIARACNIDICO, true",
            "ARANHA, ANTILOXOSCELICO, true",
            "ARANHA, ANTIESCORPIONICO, false",
            "LAGARTA, ANTILONOMICO, true",
            "LAGARTA, ANTIARACNIDICO, false",
    })
    void bastaTerUmDosSorosDoAnimal(TipoAnimal animal, TipoSoro soro, boolean esperado) {
        assertThat(servico.atende(unidade(TipoUnidade.HOSPITAL, soro), animal)).isEqualTo(esperado);
    }

    @Test
    void naoSeiPrecisaDeAntibotropicoEDeEscorpionicoOuAracnidico() {
        assertThat(servico.atende(unidade(TipoUnidade.HOSPITAL, ANTIBOTROPICO, ANTIESCORPIONICO), TipoAnimal.NAO_SEI)).isTrue();
        assertThat(servico.atende(unidade(TipoUnidade.HOSPITAL, ANTIBOTROPICO, ANTIARACNIDICO), TipoAnimal.NAO_SEI)).isTrue();
        assertThat(servico.atende(unidade(TipoUnidade.HOSPITAL, ANTIBOTROPICO), TipoAnimal.NAO_SEI)).isFalse();
        assertThat(servico.atende(unidade(TipoUnidade.HOSPITAL, ANTIESCORPIONICO, ANTIARACNIDICO), TipoAnimal.NAO_SEI)).isFalse();
    }

    @Test
    void abelhaNaoTemSoroValeHospitalOuUpa() {
        assertThat(servico.atende(unidade(TipoUnidade.HOSPITAL), TipoAnimal.ABELHA)).isTrue();
        assertThat(servico.atende(unidade(TipoUnidade.UPA), TipoAnimal.ABELHA)).isTrue();
        assertThat(servico.atende(unidade(TipoUnidade.UBS, ANTIESCORPIONICO), TipoAnimal.ABELHA)).isFalse();
    }

    @Test
    void unidadeSemSoroSoServeParaAbelha() {
        UnidadeSaude semSoro = unidade(TipoUnidade.UPA);

        for (TipoAnimal animal : TipoAnimal.values()) {
            assertThat(servico.atende(semSoro, animal)).isEqualTo(animal == TipoAnimal.ABELHA);
        }
    }

    @Test
    void sorosCompativeisTrazSoOsQueServemParaOAnimal() {
        UnidadeSaude completa = unidade(TipoUnidade.HOSPITAL,
                ANTILONOMICO, ANTIESCORPIONICO, ANTIBOTROPICO, ANTIARACNIDICO, ANTICROTALICO);

        assertThat(servico.sorosCompativeis(completa, TipoAnimal.ESCORPIAO))
                .containsExactly(ANTIESCORPIONICO, ANTIARACNIDICO);
        assertThat(servico.sorosCompativeis(completa, TipoAnimal.SERPENTE))
                .containsExactly(ANTIBOTROPICO, ANTICROTALICO);
        assertThat(servico.sorosCompativeis(completa, TipoAnimal.NAO_SEI))
                .containsExactly(ANTIESCORPIONICO, ANTIARACNIDICO, ANTIBOTROPICO);
        assertThat(servico.sorosCompativeis(completa, TipoAnimal.LAGARTA)).containsExactly(ANTILONOMICO);
        assertThat(servico.sorosCompativeis(completa, TipoAnimal.ABELHA)).isEmpty();
    }
}
