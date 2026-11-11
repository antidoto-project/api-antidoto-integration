package br.com.fiap.antidoto.service;

import br.com.fiap.antidoto.dto.ocorrencia.ResultadoUrgencia;
import br.com.fiap.antidoto.model.FaixaEtaria;
import br.com.fiap.antidoto.model.Sintoma;
import br.com.fiap.antidoto.model.TipoAnimal;
import br.com.fiap.antidoto.model.Urgencia;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Regra de urgência RN-U (MVP-00 §4.3). É a MESMA do app (utils/urgencia.ts),
 * com os mesmos 10 casos de teste: não mude sem combinar com o front. A IA nunca
 * decide a urgência.
 */
@Service
public class UrgenciaService {

    private static final Set<TipoAnimal> ANIMAIS_COM_RISCO_DE_AGRAVAMENTO =
            EnumSet.of(TipoAnimal.SERPENTE, TipoAnimal.ARANHA, TipoAnimal.LAGARTA, TipoAnimal.NAO_SEI);

    private static final Duration TEMPO_PARA_SUBIR_UM_NIVEL = Duration.ofHours(3);

    public ResultadoUrgencia calcular(
            TipoAnimal tipoAnimal,
            FaixaEtaria faixaEtaria,
            Collection<Sintoma> sintomas,
            Instant horaPicada,
            Instant agora
    ) {
        Urgencia urgencia = Urgencia.MODERADA;
        List<String> motivos = new ArrayList<>();

        // RN-U1
        if (sintomas.stream().anyMatch(Sintoma::isAlerta)) {
            urgencia = urgencia.maior(Urgencia.EMERGENCIA);
            adicionar(motivos, "Sinal de alerta");
        }
        // RN-U2
        if (tipoAnimal == TipoAnimal.ESCORPIAO && faixaEtaria == FaixaEtaria.CRIANCA) {
            urgencia = urgencia.maior(Urgencia.EMERGENCIA);
            adicionar(motivos, "Escorpião em criança");
        }
        // RN-U3
        if (ANIMAIS_COM_RISCO_DE_AGRAVAMENTO.contains(tipoAnimal)) {
            urgencia = urgencia.maior(Urgencia.ALTA);
            adicionar(motivos, "Animal com risco de agravamento");
        }
        // RN-U4
        if (faixaEtaria == FaixaEtaria.CRIANCA || faixaEtaria == FaixaEtaria.IDOSO) {
            urgencia = urgencia.maior(Urgencia.ALTA);
            adicionar(motivos, "Criança ou idoso");
        }
        // RN-U5 (por último): 3 h ou mais desde a picada sobe um nível
        boolean passouDoTempo = Duration.between(horaPicada, agora).compareTo(TEMPO_PARA_SUBIR_UM_NIVEL) >= 0;
        if (passouDoTempo && urgencia != Urgencia.EMERGENCIA) {
            urgencia = urgencia.proxima();
            motivos.add("Mais de 3 horas desde a picada");
        }
        // RN-U6
        if (motivos.isEmpty()) {
            motivos.add("Avaliação necessária");
        }

        return new ResultadoUrgencia(urgencia, List.copyOf(motivos), urgencia == Urgencia.EMERGENCIA);
    }

    private static void adicionar(List<String> motivos, String motivo) {
        if (!motivos.contains(motivo)) {
            motivos.add(motivo);
        }
    }
}
