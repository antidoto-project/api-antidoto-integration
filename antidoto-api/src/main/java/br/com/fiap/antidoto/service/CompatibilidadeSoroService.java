package br.com.fiap.antidoto.service;

import br.com.fiap.antidoto.model.TipoAnimal;
import br.com.fiap.antidoto.model.TipoSoro;
import br.com.fiap.antidoto.model.TipoUnidade;
import br.com.fiap.antidoto.model.UnidadeSaude;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static br.com.fiap.antidoto.model.TipoSoro.*;

/**
 * Compatibilidade animal → soro (RN-S, MVP-00 §4.4): se a unidade atende o animal
 * e quais dos soros dela servem (RN-S3).
 */
@Service
public class CompatibilidadeSoroService {

    /** Basta ter UM destes soros. */
    private static final Map<TipoAnimal, Set<TipoSoro>> SOROS_POR_ANIMAL = Map.of(
            TipoAnimal.ESCORPIAO, EnumSet.of(ANTIESCORPIONICO, ANTIARACNIDICO),
            TipoAnimal.SERPENTE, EnumSet.of(ANTIBOTROPICO, ANTICROTALICO, ANTIELAPIDICO, ANTILAQUETICO),
            TipoAnimal.ARANHA, EnumSet.of(ANTIARACNIDICO, ANTILOXOSCELICO),
            TipoAnimal.LAGARTA, EnumSet.of(ANTILONOMICO)
    );

    /** NAO_SEI: os soros que entram na regra (ANTIBOTROPICO e ANTIESCORPIONICO ou ANTIARACNIDICO). */
    private static final Set<TipoSoro> SOROS_NAO_SEI = EnumSet.of(ANTIBOTROPICO, ANTIESCORPIONICO, ANTIARACNIDICO);

    /** ABELHA: não existe soro; vale qualquer hospital ou UPA (atendimento de urgência). */
    private static final Set<TipoUnidade> TIPOS_PARA_ABELHA = EnumSet.of(TipoUnidade.HOSPITAL, TipoUnidade.UPA);

    public boolean atende(UnidadeSaude unidade, TipoAnimal animal) {
        Set<TipoSoro> soros = unidade.getSoros();
        return switch (animal) {
            case ABELHA -> TIPOS_PARA_ABELHA.contains(unidade.getTipo());
            case NAO_SEI -> soros.contains(ANTIBOTROPICO)
                    && (soros.contains(ANTIESCORPIONICO) || soros.contains(ANTIARACNIDICO));
            default -> soros.stream().anyMatch(SOROS_POR_ANIMAL.get(animal)::contains);
        };
    }

    /** RN-S3: só os soros da unidade que servem para o animal (vazio na ABELHA), na ordem do enum. */
    public List<TipoSoro> sorosCompativeis(UnidadeSaude unidade, TipoAnimal animal) {
        Set<TipoSoro> queServem = switch (animal) {
            case ABELHA -> Set.of();
            case NAO_SEI -> SOROS_NAO_SEI;
            default -> SOROS_POR_ANIMAL.get(animal);
        };
        return unidade.getSoros().stream()
                .filter(queServem::contains)
                .sorted()
                .toList();
    }
}
