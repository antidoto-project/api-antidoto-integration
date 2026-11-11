package br.com.fiap.antidoto.mensageria;

import br.com.fiap.antidoto.ia.ResumoModeloPadrao;
import br.com.fiap.antidoto.model.Ocorrencia;
import br.com.fiap.antidoto.model.StatusResumo;
import br.com.fiap.antidoto.repository.OcorrenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.Instant;

/**
 * Gera o resumo em segundo plano (MVP-01 §9, modo sem RabbitMQ): roda depois do
 * commit do POST, em outra thread, então o app recebe o 201 com PENDENTE na hora
 * e consulta até o status mudar. Idempotente: se a ocorrência já não está
 * PENDENTE, não faz nada.
 */
@Component
public class ResumoOcorrenciaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResumoOcorrenciaConsumer.class);

    private final OcorrenciaRepository ocorrenciaRepository;
    private final ResumoModeloPadrao resumoModeloPadrao;
    private final Clock relogio;

    public ResumoOcorrenciaConsumer(
            OcorrenciaRepository ocorrenciaRepository,
            ResumoModeloPadrao resumoModeloPadrao,
            Clock relogio
    ) {
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.resumoModeloPadrao = resumoModeloPadrao;
        this.relogio = relogio;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void gerarResumo(OcorrenciaCriadaEvento evento) {
        ocorrenciaRepository.findById(evento.ocorrenciaId())
                .filter(ocorrencia -> ocorrencia.getStatusResumo() == StatusResumo.PENDENTE)
                .ifPresent(this::preencherResumo);
    }

    private void preencherResumo(Ocorrencia ocorrencia) {
        long inicio = System.nanoTime();
        ocorrencia.setResumo(resumoModeloPadrao.montar(ocorrencia, Instant.now(relogio)));
        ocorrencia.setStatusResumo(StatusResumo.MODELO_PADRAO);
        // Nunca logar o texto do resumo: só o id, o status e o tempo (MVP-01 §8)
        log.info("Resumo da ocorrência {}: {} em {} ms", ocorrencia.getId(), ocorrencia.getStatusResumo(),
                (System.nanoTime() - inicio) / 1_000_000);
    }
}
