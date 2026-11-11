package br.com.fiap.antidoto.service;

import br.com.fiap.antidoto.dto.PaginaResponse;
import br.com.fiap.antidoto.dto.unidade.UnidadeProximaResponse;
import br.com.fiap.antidoto.dto.unidade.UnidadeResponse;
import br.com.fiap.antidoto.exception.RecursoNaoEncontradoException;
import br.com.fiap.antidoto.mapper.UnidadeMapper;
import br.com.fiap.antidoto.model.TipoAnimal;
import br.com.fiap.antidoto.model.UnidadeSaude;
import br.com.fiap.antidoto.repository.UnidadeSaudeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Unidades de saúde (MVP-01 §6.2). */
@Service
public class UnidadeService {

    private final UnidadeSaudeRepository unidadeRepository;
    private final CompatibilidadeSoroService compatibilidadeSoroService;
    private final DistanciaService distanciaService;
    private final UnidadeMapper unidadeMapper;

    public UnidadeService(
            UnidadeSaudeRepository unidadeRepository,
            CompatibilidadeSoroService compatibilidadeSoroService,
            DistanciaService distanciaService,
            UnidadeMapper unidadeMapper
    ) {
        this.unidadeRepository = unidadeRepository;
        this.compatibilidadeSoroService = compatibilidadeSoroService;
        this.distanciaService = distanciaService;
        this.unidadeMapper = unidadeMapper;
    }

    /**
     * RN-S1 a RN-S4: as ativas que atendem o animal, da mais perto para a mais longe,
     * no máximo "limite". Poucas unidades: filtra e ordena em memória (MVP-01 §6.2).
     * Lista vazia é resposta válida (o app mostra o 192).
     */
    @Transactional(readOnly = true)
    public List<UnidadeProximaResponse> buscarProximas(
            double latitude, double longitude, TipoAnimal animal, int limite) {
        record UnidadeComDistancia(UnidadeSaude unidade, double km) {
        }

        return unidadeRepository.findByAtivaTrue().stream()
                .filter(unidade -> compatibilidadeSoroService.atende(unidade, animal))
                .map(unidade -> new UnidadeComDistancia(unidade, distanciaService.calcularKm(
                        latitude, longitude, unidade.getLatitude(), unidade.getLongitude())))
                .sorted(Comparator.comparingDouble(UnidadeComDistancia::km)
                        .thenComparing(item -> item.unidade().getId()))
                .limit(limite)
                .map(item -> unidadeMapper.paraProxima(
                        item.unidade(),
                        distanciaService.arredondar(item.km()),
                        compatibilidadeSoroService.sorosCompativeis(item.unidade(), animal)))
                .toList();
    }

    /** Só as ativas, por nome. */
    @Transactional(readOnly = true)
    public PaginaResponse<UnidadeResponse> listar(int pagina, int tamanho) {
        var pageable = PageRequest.of(pagina, tamanho, Sort.by("nome"));
        return PaginaResponse.de(unidadeRepository.findByAtivaTrue(pageable).map(unidadeMapper::paraResponse));
    }

    @Transactional(readOnly = true)
    public UnidadeResponse buscar(Long id) {
        return unidadeRepository.findByIdAndAtivaTrue(id)
                .map(unidadeMapper::paraResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade não encontrada."));
    }
}
