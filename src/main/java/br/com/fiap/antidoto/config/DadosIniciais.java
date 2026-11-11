package br.com.fiap.antidoto.config;

import br.com.fiap.antidoto.dto.ocorrencia.ResultadoUrgencia;
import br.com.fiap.antidoto.ia.ResumoModeloPadrao;
import br.com.fiap.antidoto.mapper.OcorrenciaMapper;
import br.com.fiap.antidoto.model.Avistamento;
import br.com.fiap.antidoto.model.FaixaEtaria;
import br.com.fiap.antidoto.model.Ocorrencia;
import br.com.fiap.antidoto.model.Sintoma;
import br.com.fiap.antidoto.model.StatusResumo;
import br.com.fiap.antidoto.model.LocalAvistamento;
import br.com.fiap.antidoto.model.Perfil;
import br.com.fiap.antidoto.model.TipoAnimal;
import br.com.fiap.antidoto.model.TipoSoro;
import br.com.fiap.antidoto.model.TipoUnidade;
import br.com.fiap.antidoto.model.UnidadeSaude;
import br.com.fiap.antidoto.model.Usuario;
import br.com.fiap.antidoto.repository.AvistamentoRepository;
import br.com.fiap.antidoto.repository.OcorrenciaRepository;
import br.com.fiap.antidoto.repository.UnidadeSaudeRepository;
import br.com.fiap.antidoto.repository.UsuarioRepository;
import br.com.fiap.antidoto.service.UrgenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static br.com.fiap.antidoto.model.TipoSoro.*;
import static br.com.fiap.antidoto.model.TipoUnidade.*;

/**
 * Dados de teste (MVP-01 §10), criados na subida só se ainda não existirem: os
 * usuários, as unidades de saúde (fictícias), os 3 avistamentos e a ocorrência de
 * exemplo do cidadão.
 * Os mesmos usuários do Java do grupo, para o app e o vídeo funcionarem com os dois.
 * Desligue com CARREGAR_DADOS_INICIAIS=false.
 */
@Component
public class DadosIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    /** Senha dos usuários de teste: pública no README (dados fictícios). */
    static final String SENHA_DE_TESTE = "Teste@123";

    private record UnidadeDeExemplo(
            String nome, TipoUnidade tipo, String endereco, String telefone,
            double latitude, double longitude, boolean atende24h, Set<TipoSoro> soros) {
    }

    /**
     * 12 unidades FICTÍCIAS em São Paulo (MVP-01 §10), com soros que cobrem todas as
     * linhas da RN-S: uma sem soro, UBS (que não entram na ABELHA) e hospitais com
     * ANTIBOTROPICO + ANTIESCORPIONICO/ANTIARACNIDICO (o NAO_SEI).
     */
    private static final List<UnidadeDeExemplo> UNIDADES = List.of(
            new UnidadeDeExemplo("Hospital Exemplo Centro", HOSPITAL, "Rua Exemplo, 100 - Sé, São Paulo/SP",
                    "1130000001", -23.5505, -46.6333, true,
                    EnumSet.of(ANTIESCORPIONICO, ANTIARACNIDICO, ANTIBOTROPICO, ANTICROTALICO)),
            new UnidadeDeExemplo("Hospital Exemplo Paulista", HOSPITAL, "Avenida Exemplo, 1500 - Bela Vista, São Paulo/SP",
                    "1130000002", -23.5629, -46.6544, true,
                    EnumSet.of(ANTIESCORPIONICO, ANTIBOTROPICO, ANTIELAPIDICO, ANTILAQUETICO)),
            new UnidadeDeExemplo("UPA Exemplo Consolação", UPA, "Rua Exemplo, 250 - Consolação, São Paulo/SP",
                    "1130000003", -23.5530, -46.6600, true,
                    EnumSet.of(ANTIESCORPIONICO)),
            new UnidadeDeExemplo("UPA Exemplo Zona Sul", UPA, "Avenida Exemplo, 800 - Santo Amaro, São Paulo/SP",
                    "1130000004", -23.6500, -46.7000, true,
                    EnumSet.of(ANTIARACNIDICO, ANTILOXOSCELICO)),
            new UnidadeDeExemplo("Hospital Exemplo Zona Norte", HOSPITAL, "Rua Exemplo, 40 - Santana, São Paulo/SP",
                    "1130000005", -23.4800, -46.6200, true,
                    EnumSet.of(ANTIBOTROPICO, ANTICROTALICO)),
            new UnidadeDeExemplo("Hospital Exemplo Zona Leste", HOSPITAL, "Rua Exemplo, 900 - Tatuapé, São Paulo/SP",
                    "1130000006", -23.5400, -46.5200, true,
                    EnumSet.of(ANTIESCORPIONICO, ANTILONOMICO)),
            new UnidadeDeExemplo("UBS Exemplo Vila Mariana", UBS, "Rua Exemplo, 75 - Vila Mariana, São Paulo/SP",
                    "1130000007", -23.5880, -46.6390, false,
                    EnumSet.of(ANTIESCORPIONICO)),
            new UnidadeDeExemplo("UPA Exemplo Pinheiros", UPA, "Rua Exemplo, 320 - Pinheiros, São Paulo/SP",
                    "1130000008", -23.5670, -46.6920, true,
                    EnumSet.noneOf(TipoSoro.class)),
            new UnidadeDeExemplo("UBS Exemplo Butantã", UBS, "Avenida Exemplo, 60 - Butantã, São Paulo/SP",
                    "1130000009", -23.5720, -46.7080, false,
                    EnumSet.noneOf(TipoSoro.class)),
            new UnidadeDeExemplo("Hospital Exemplo Lapa", HOSPITAL, "Rua Exemplo, 510 - Lapa, São Paulo/SP",
                    "1130000010", -23.5250, -46.7050, true,
                    EnumSet.of(ANTIARACNIDICO, ANTIBOTROPICO)),
            new UnidadeDeExemplo("UPA Exemplo Ipiranga", UPA, "Rua Exemplo, 1200 - Ipiranga, São Paulo/SP",
                    "1130000011", -23.5890, -46.6100, true,
                    EnumSet.of(ANTIELAPIDICO, ANTILAQUETICO)),
            new UnidadeDeExemplo("Hospital Exemplo Jabaquara", HOSPITAL, "Avenida Exemplo, 2000 - Jabaquara, São Paulo/SP",
                    "1130000012", -23.6450, -46.6400, true,
                    EnumSet.of(ANTILOXOSCELICO, ANTILONOMICO))
    );

    private record AvistamentoDeExemplo(
            TipoAnimal tipoAnimal, LocalAvistamento local, double latitude, double longitude,
            Duration ha, String observacao) {
    }

    /** Perto da Avenida Paulista, onde o app costuma ser testado. */
    private static final List<AvistamentoDeExemplo> AVISTAMENTOS = List.of(
            new AvistamentoDeExemplo(TipoAnimal.ESCORPIAO, LocalAvistamento.CASA, -23.5640, -46.6520,
                    Duration.ofDays(2), "Vi perto do ralo do banheiro"),
            new AvistamentoDeExemplo(TipoAnimal.ARANHA, LocalAvistamento.QUINTAL, -23.5700, -46.6450,
                    Duration.ofDays(5), "Atrás dos tijolos encostados no muro"),
            new AvistamentoDeExemplo(TipoAnimal.SERPENTE, LocalAvistamento.VIA_PUBLICA, -23.5850, -46.6600,
                    Duration.ofDays(10), null)
    );

    private final boolean carregar;
    private final UsuarioRepository usuarioRepository;
    private final UnidadeSaudeRepository unidadeRepository;
    private final AvistamentoRepository avistamentoRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final UrgenciaService urgenciaService;
    private final ResumoModeloPadrao resumoModeloPadrao;
    private final PasswordEncoder codificadorDeSenha;
    private final Clock relogio;

    public DadosIniciais(
            @Value("${antidoto.dados-iniciais}") boolean carregar,
            UsuarioRepository usuarioRepository,
            UnidadeSaudeRepository unidadeRepository,
            AvistamentoRepository avistamentoRepository,
            OcorrenciaRepository ocorrenciaRepository,
            UrgenciaService urgenciaService,
            ResumoModeloPadrao resumoModeloPadrao,
            PasswordEncoder codificadorDeSenha,
            Clock relogio
    ) {
        this.carregar = carregar;
        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.avistamentoRepository = avistamentoRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.urgenciaService = urgenciaService;
        this.resumoModeloPadrao = resumoModeloPadrao;
        this.codificadorDeSenha = codificadorDeSenha;
        this.relogio = relogio;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (!carregar) {
            return;
        }
        criarUsuarioSeNaoExistir("cidadao@sospeconha.dev", "Ana Souza", "11999990000", Perfil.CIDADAO);
        criarUsuarioSeNaoExistir("gestor@sospeconha.dev", "Gestor de Teste", null, Perfil.GESTOR);
        UNIDADES.forEach(this::criarUnidadeSeNaoExistir);
        usuarioRepository.findByEmail("cidadao@sospeconha.dev").ifPresent(cidadao -> {
            criarAvistamentosSeNaoTiver(cidadao);
            criarOcorrenciaSeNaoTiver(cidadao);
        });
        log.info("Dados iniciais conferidos (usuários de teste, {} unidades fictícias e os exemplos do cidadão)",
                UNIDADES.size());
    }

    private void criarUsuarioSeNaoExistir(String email, String nome, String telefone, Perfil perfil) {
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(codificadorDeSenha.encode(SENHA_DE_TESTE));
        usuario.setTelefone(telefone);
        usuario.setPerfil(perfil);
        usuario.setCriadoEm(Instant.now(relogio).truncatedTo(ChronoUnit.MILLIS));
        usuarioRepository.save(usuario);
    }

    private void criarUnidadeSeNaoExistir(UnidadeDeExemplo exemplo) {
        if (unidadeRepository.existsByNome(exemplo.nome())) {
            return;
        }
        UnidadeSaude unidade = new UnidadeSaude();
        unidade.setNome(exemplo.nome());
        unidade.setTipo(exemplo.tipo());
        unidade.setEndereco(exemplo.endereco());
        unidade.setTelefone(exemplo.telefone());
        unidade.setLatitude(exemplo.latitude());
        unidade.setLongitude(exemplo.longitude());
        unidade.setAtende24h(exemplo.atende24h());
        unidade.setAtiva(true);
        Set<TipoSoro> soros = EnumSet.noneOf(TipoSoro.class);
        soros.addAll(exemplo.soros());
        unidade.setSoros(soros);
        unidade.setAtualizadoEm(Instant.now(relogio).truncatedTo(ChronoUnit.MILLIS));
        unidadeRepository.save(unidade);
    }

    private void criarAvistamentosSeNaoTiver(Usuario cidadao) {
        if (avistamentoRepository.existsByUsuarioId(cidadao.getId())) {
            return;
        }
        Instant agora = Instant.now(relogio).truncatedTo(ChronoUnit.MILLIS);
        for (AvistamentoDeExemplo exemplo : AVISTAMENTOS) {
            Avistamento avistamento = new Avistamento();
            avistamento.setUsuario(cidadao);
            avistamento.setTipoAnimal(exemplo.tipoAnimal());
            avistamento.setLocal(exemplo.local());
            avistamento.setLatitude(exemplo.latitude());
            avistamento.setLongitude(exemplo.longitude());
            avistamento.setDataHora(agora.minus(exemplo.ha()));
            avistamento.setObservacao(exemplo.observacao());
            avistamento.setCriadoEm(agora.minus(exemplo.ha()).plus(Duration.ofMinutes(2)));
            avistamentoRepository.save(avistamento);
        }
    }

    /**
     * Uma ocorrência de ontem, já com o resumo pronto (modelo padrão), para o
     * histórico do app não começar vazio. A urgência sai da mesma RN-U da API.
     */
    private void criarOcorrenciaSeNaoTiver(Usuario cidadao) {
        if (ocorrenciaRepository.existsByUsuarioId(cidadao.getId())) {
            return;
        }
        Instant registro = Instant.now(relogio).truncatedTo(ChronoUnit.MILLIS).minus(Duration.ofDays(1));
        Instant horaPicada = registro.minus(Duration.ofMinutes(20));
        Set<Sintoma> sintomas = EnumSet.of(Sintoma.DOR_LOCAL, Sintoma.INCHACO_LOCAL);
        ResultadoUrgencia resultado = urgenciaService.calcular(
                TipoAnimal.ESCORPIAO, FaixaEtaria.ADULTO, sintomas, horaPicada, registro);

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setUsuario(cidadao);
        unidadeRepository.findByNome("Hospital Exemplo Paulista").ifPresent(ocorrencia::setUnidade);
        ocorrencia.setTipoAnimal(TipoAnimal.ESCORPIAO);
        ocorrencia.setFaixaEtaria(FaixaEtaria.ADULTO);
        ocorrencia.setSintomas(sintomas);
        ocorrencia.setHoraPicada(horaPicada);
        ocorrencia.setLatitude(-23.5640);
        ocorrencia.setLongitude(-46.6520);
        ocorrencia.setUrgencia(resultado.urgencia());
        ocorrencia.setMotivos(OcorrenciaMapper.motivosParaColuna(resultado.motivos()));
        ocorrencia.setCriadoEm(registro);
        ocorrencia.setResumo(resumoModeloPadrao.montar(ocorrencia, registro));
        ocorrencia.setStatusResumo(StatusResumo.MODELO_PADRAO);
        ocorrenciaRepository.save(ocorrencia);
    }
}
