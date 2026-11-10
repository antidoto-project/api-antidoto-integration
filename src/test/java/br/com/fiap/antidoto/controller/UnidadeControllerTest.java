package br.com.fiap.antidoto.controller;

import br.com.fiap.antidoto.model.TipoSoro;
import br.com.fiap.antidoto.model.TipoUnidade;
import br.com.fiap.antidoto.model.UnidadeSaude;
import br.com.fiap.antidoto.repository.UnidadeSaudeRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** GET /api/unidades/proximas e a lista pública, com as 12 unidades fictícias dos dados iniciais. */
@SpringBootTest
@AutoConfigureMockMvc
class UnidadeControllerTest {

    /** Avenida Paulista (MASP), onde o app costuma ser testado. */
    private static final String LATITUDE = "-23.5614";
    private static final String LONGITUDE = "-46.6559";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UnidadeSaudeRepository unidadeRepository;

    private List<Map<String, Object>> proximas(String tipoAnimal, String limite) throws Exception {
        String json = mvc.perform(get("/api/unidades/proximas")
                        .param("latitude", LATITUDE)
                        .param("longitude", LONGITUDE)
                        .param("tipoAnimal", tipoAnimal)
                        .param("limite", limite))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$");
    }

    private static double distancia(Map<String, Object> unidade) {
        return ((Number) unidade.get("distanciaKm")).doubleValue();
    }

    @Test
    void escorpiaoSemLoginTrazAs5MaisPertoComOsSorosQueServem() throws Exception {
        List<Map<String, Object>> unidades = proximas("ESCORPIAO", "5");

        assertThat(unidades).hasSize(5);
        assertThat(unidades.getFirst().get("nome")).isEqualTo("Hospital Exemplo Paulista");
        // RN-S2: da mais perto para a mais longe
        for (int i = 1; i < unidades.size(); i++) {
            assertThat(distancia(unidades.get(i))).isGreaterThanOrEqualTo(distancia(unidades.get(i - 1)));
        }
        // RN-S3: só os soros que servem para escorpião; distância com 1 casa
        for (Map<String, Object> unidade : unidades) {
            assertThat((List<String>) unidade.get("sorosCompativeis"))
                    .isNotEmpty()
                    .isSubsetOf("ANTIESCORPIONICO", "ANTIARACNIDICO");
            double km = distancia(unidade);
            assertThat(km * 10).isEqualTo(Math.rint(km * 10));
        }
        assertThat(unidades.getFirst()).containsKeys(
                "id", "nome", "tipo", "endereco", "telefone", "latitude", "longitude", "atende24h");
    }

    @Test
    void abelhaTrazSoHospitaisEUpasSemSoro() throws Exception {
        List<Map<String, Object>> unidades = proximas("ABELHA", "10");

        assertThat(unidades).isNotEmpty();
        for (Map<String, Object> unidade : unidades) {
            assertThat(unidade.get("tipo")).isIn("HOSPITAL", "UPA");
            assertThat((List<?>) unidade.get("sorosCompativeis")).isEmpty();
        }
        assertThat(unidades).extracting(unidade -> unidade.get("nome")).contains("UPA Exemplo Pinheiros");
    }

    @Test
    void naoSeiTrazSoQuemTemAntibotropicoEEscorpionicoOuAracnidico() throws Exception {
        List<Map<String, Object>> unidades = proximas("NAO_SEI", "10");

        assertThat(unidades).extracting(unidade -> unidade.get("nome")).containsExactlyInAnyOrder(
                "Hospital Exemplo Centro", "Hospital Exemplo Paulista", "Hospital Exemplo Lapa");
        for (Map<String, Object> unidade : unidades) {
            assertThat((List<String>) unidade.get("sorosCompativeis")).contains("ANTIBOTROPICO");
        }
    }

    @Test
    void unidadeDesativadaNaoAparece() throws Exception {
        UnidadeSaude desativada = new UnidadeSaude();
        desativada.setNome("Hospital Desativado no Ponto Exato");
        desativada.setTipo(TipoUnidade.HOSPITAL);
        desativada.setEndereco("Rua Teste, 1");
        desativada.setTelefone("1130009999");
        desativada.setLatitude(Double.valueOf(LATITUDE));
        desativada.setLongitude(Double.valueOf(LONGITUDE));
        desativada.setAtende24h(true);
        desativada.setAtiva(false);
        desativada.setSoros(EnumSet.allOf(TipoSoro.class));
        desativada.setAtualizadoEm(Instant.now());
        unidadeRepository.save(desativada);

        assertThat(proximas("LAGARTA", "10")).extracting(unidade -> unidade.get("nome"))
                .doesNotContain("Hospital Desativado no Ponto Exato");
    }

    @Test
    void tokenVencidoNoCabecalhoNaoAtrapalhaOSos() throws Exception {
        mvc.perform(get("/api/unidades/proximas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-vencido")
                        .param("latitude", LATITUDE).param("longitude", LONGITUDE).param("tipoAnimal", "SERPENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(5))));
    }

    @Test
    void parametrosInvalidosApontamOCampo() throws Exception {
        mvc.perform(get("/api/unidades/proximas")
                        .param("latitude", "-40").param("longitude", LONGITUDE).param("tipoAnimal", "ESCORPIAO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("latitude"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("Latitude fora do Brasil"));

        mvc.perform(get("/api/unidades/proximas")
                        .param("latitude", LATITUDE).param("longitude", LONGITUDE).param("tipoAnimal", "PEIXE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("tipoAnimal"));

        mvc.perform(get("/api/unidades/proximas")
                        .param("latitude", LATITUDE).param("longitude", LONGITUDE).param("tipoAnimal", "ESCORPIAO")
                        .param("limite", "11"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("limite"));

        mvc.perform(get("/api/unidades/proximas").param("latitude", LATITUDE).param("longitude", LONGITUDE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("tipoAnimal"));
    }

    @Test
    void listaPublicaPaginadaPorNomeEDetalhe() throws Exception {
        String json = mvc.perform(get("/api/unidades").param("pagina", "0").param("tamanho", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(5)))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(5))
                .andExpect(jsonPath("$.totalElementos").value(12))
                .andExpect(jsonPath("$.totalPaginas").value(3))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Hospital Exemplo Centro"))
                .andExpect(jsonPath("$.conteudo[0].ativa").value(true))
                .andReturn().getResponse().getContentAsString();

        Integer id = JsonPath.read(json, "$.conteudo[0].id");
        mvc.perform(get("/api/unidades/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.soros").isArray())
                .andExpect(jsonPath("$.atualizadoEm").exists());

        mvc.perform(get("/api/unidades/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Unidade não encontrada."));

        mvc.perform(get("/api/unidades").param("tamanho", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("tamanho"));
    }
}
