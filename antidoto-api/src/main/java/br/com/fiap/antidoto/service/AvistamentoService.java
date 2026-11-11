package br.com.fiap.antidoto.service;

import br.com.fiap.antidoto.dto.PaginaResponse;
import br.com.fiap.antidoto.dto.avistamento.AvistamentoRequest;
import br.com.fiap.antidoto.dto.avistamento.AvistamentoResponse;
import br.com.fiap.antidoto.exception.RecursoNaoEncontradoException;
import br.com.fiap.antidoto.exception.RegraDeNegocioException;
import br.com.fiap.antidoto.mapper.AvistamentoMapper;
import br.com.fiap.antidoto.model.Avistamento;
import br.com.fiap.antidoto.repository.AvistamentoRepository;
import br.com.fiap.antidoto.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * CRUD de avistamentos (MVP-01 §6.4), o CRUD do app. RN-G5: tudo só do dono;
 * de outro usuário ou inexistente, 404.
 */
@Service
public class AvistamentoService {

    /** Tolerância para o relógio do celular adiantado. */
    private static final Duration TOLERANCIA_DO_FUTURO = Duration.ofMinutes(5);

    private final AvistamentoRepository avistamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AvistamentoMapper avistamentoMapper;
    private final Clock relogio;

    public AvistamentoService(
            AvistamentoRepository avistamentoRepository,
            UsuarioRepository usuarioRepository,
            AvistamentoMapper avistamentoMapper,
            Clock relogio
    ) {
        this.avistamentoRepository = avistamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.avistamentoMapper = avistamentoMapper;
        this.relogio = relogio;
    }

    @Transactional
    public AvistamentoResponse criar(Long idUsuario, AvistamentoRequest requisicao) {
        conferirDataHora(requisicao);

        Avistamento avistamento = new Avistamento();
        avistamento.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        preencher(avistamento, requisicao);
        avistamento.setCriadoEm(agora());

        return avistamentoMapper.paraResponse(avistamentoRepository.save(avistamento));
    }

    /** Os do usuário logado, mais recentes primeiro. */
    @Transactional(readOnly = true)
    public PaginaResponse<AvistamentoResponse> listarMeus(Long idUsuario, int pagina, int tamanho) {
        var pageable = PageRequest.of(pagina, tamanho,
                Sort.by(Sort.Order.desc("criadoEm"), Sort.Order.desc("id")));
        return PaginaResponse.de(avistamentoRepository.findByUsuarioId(idUsuario, pageable)
                .map(avistamentoMapper::paraResponse));
    }

    @Transactional(readOnly = true)
    public AvistamentoResponse buscar(Long idUsuario, Long id) {
        return avistamentoMapper.paraResponse(carregarDoDono(idUsuario, id));
    }

    @Transactional
    public AvistamentoResponse atualizar(Long idUsuario, Long id, AvistamentoRequest requisicao) {
        Avistamento avistamento = carregarDoDono(idUsuario, id);
        conferirDataHora(requisicao);

        preencher(avistamento, requisicao);
        avistamento.setAtualizadoEm(agora());

        return avistamentoMapper.paraResponse(avistamento);
    }

    /** Exclusão física (MVP-01 §6.4). */
    @Transactional
    public void excluir(Long idUsuario, Long id) {
        avistamentoRepository.delete(carregarDoDono(idUsuario, id));
    }

    private Avistamento carregarDoDono(Long idUsuario, Long id) {
        return avistamentoRepository.findByIdAndUsuarioId(id, idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avistamento não encontrado."));
    }

    private void conferirDataHora(AvistamentoRequest requisicao) {
        if (requisicao.dataHora().isAfter(Instant.now(relogio).plus(TOLERANCIA_DO_FUTURO))) {
            throw new RegraDeNegocioException("dataHora", "A data e a hora não podem estar no futuro");
        }
    }

    private static void preencher(Avistamento avistamento, AvistamentoRequest requisicao) {
        avistamento.setTipoAnimal(requisicao.tipoAnimal());
        avistamento.setLocal(requisicao.local());
        avistamento.setLatitude(requisicao.latitude());
        avistamento.setLongitude(requisicao.longitude());
        avistamento.setDataHora(requisicao.dataHora().truncatedTo(ChronoUnit.MILLIS));
        avistamento.setObservacao(requisicao.observacao());
    }

    private Instant agora() {
        return Instant.now(relogio).truncatedTo(ChronoUnit.MILLIS);
    }
}
