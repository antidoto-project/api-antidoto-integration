package br.com.fiap.antidoto.controller;

import br.com.fiap.antidoto.ApoioDeTeste;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O CRUD de avistamentos do app (MVP-01 §6.4), com o dono de cada um (RN-G5). */
@SpringBootTest
@AutoConfigureMockMvc
class AvistamentoControllerTest {

    @Autowired
    private MockMvc mvc;

    private static String corpo(String tipoAnimal, String local, Instant dataHora, String observacao) {
        String obs = observacao == null ? "null" : "\"" + observacao + "\"";
        return """
                {"tipoAnimal":"%s","local":"%s","latitude":-23.5614,"longitude":-46.6559,"dataHora":"%s","observacao":%s}
                """.formatted(tipoAnimal, local, dataHora, obs);
    }

    private static Instant agora() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    private ResultActions criar(String token, String json) throws Exception {
        return mvc.perform(post("/api/avistamentos").header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long criarERetornarId(String token) throws Exception {
        String resposta = criar(token, corpo("ESCORPIAO", "CASA", agora(), "Vi perto do ralo"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    @Test
    void crudCompletoDoDono() throws Exception {
        String token = ApoioDeTeste.cadastrarEEntrar(mvc);
        Instant dataHora = agora().minus(Duration.ofHours(1));

        // Criar
        String criado = criar(token, corpo("ESCORPIAO", "CASA", dataHora, "  Vi perto do ralo  "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tipoAnimal").value("ESCORPIAO"))
                .andExpect(jsonPath("$.local").value("CASA"))
                .andExpect(jsonPath("$.latitude").value(-23.5614))
                .andExpect(jsonPath("$.longitude").value(-46.6559))
                .andExpect(jsonPath("$.dataHora").value(dataHora.toString()))
                .andExpect(jsonPath("$.observacao").value("Vi perto do ralo"))
                .andExpect(jsonPath("$.criadoEm").exists())
                .andExpect(jsonPath("$.atualizadoEm").isEmpty())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(criado, "$.id")).longValue();

        // Ler
        mvc.perform(get("/api/avistamentos/{id}", id).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observacao").value("Vi perto do ralo"));

        // Editar (o app manda a dataHora original); observação em branco vira null
        mvc.perform(put("/api/avistamentos/{id}", id).header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ARANHA", "QUINTAL", dataHora, "   ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoAnimal").value("ARANHA"))
                .andExpect(jsonPath("$.local").value("QUINTAL"))
                .andExpect(jsonPath("$.dataHora").value(dataHora.toString()))
                .andExpect(jsonPath("$.observacao").isEmpty())
                .andExpect(jsonPath("$.atualizadoEm").exists());

        // Excluir
        mvc.perform(delete("/api/avistamentos/{id}", id).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/avistamentos/{id}", id).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Avistamento não encontrado."));
    }

    @Test
    void doOutroUsuarioEmTudoE404() throws Exception {
        String dona = ApoioDeTeste.cadastrarEEntrar(mvc);
        String outra = ApoioDeTeste.cadastrarEEntrar(mvc);
        long id = criarERetornarId(dona);

        mvc.perform(get("/api/avistamentos/{id}", id).header(HttpHeaders.AUTHORIZATION, outra))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/avistamentos/{id}", id).header(HttpHeaders.AUTHORIZATION, outra)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("SERPENTE", "OUTRO", agora(), "invasão")))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/avistamentos/{id}", id).header(HttpHeaders.AUTHORIZATION, outra))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/avistamentos/meus").header(HttpHeaders.AUTHORIZATION, outra))
                .andExpect(jsonPath("$.totalElementos").value(0));

        // Continua intacto para a dona
        mvc.perform(get("/api/avistamentos/{id}", id).header(HttpHeaders.AUTHORIZATION, dona))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoAnimal").value("ESCORPIAO"));
    }

    @Test
    void semLoginE401() throws Exception {
        mvc.perform(get("/api/avistamentos/meus")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/avistamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ESCORPIAO", "CASA", agora(), null)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meusMaisRecentesPrimeiroEPaginados() throws Exception {
        String token = ApoioDeTeste.cadastrarEEntrar(mvc);
        long primeiro = criarERetornarId(token);
        long segundo = criarERetornarId(token);
        long terceiro = criarERetornarId(token);

        mvc.perform(get("/api/avistamentos/meus").header(HttpHeaders.AUTHORIZATION, token)
                        .param("pagina", "0").param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(2)))
                .andExpect(jsonPath("$.conteudo[0].id").value(terceiro))
                .andExpect(jsonPath("$.conteudo[1].id").value(segundo))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(2))
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2));

        mvc.perform(get("/api/avistamentos/meus").header(HttpHeaders.AUTHORIZATION, token)
                        .param("pagina", "1").param("tamanho", "2"))
                .andExpect(jsonPath("$.conteudo[0].id").value(primeiro));

        mvc.perform(get("/api/avistamentos/meus").header(HttpHeaders.AUTHORIZATION, token).param("tamanho", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("tamanho"));
    }

    @Test
    void dataHoraNoFuturoSoComAToleranciaDe5Minutos() throws Exception {
        String token = ApoioDeTeste.cadastrarEEntrar(mvc);

        criar(token, corpo("ESCORPIAO", "CASA", agora().plus(Duration.ofMinutes(3)), null))
                .andExpect(status().isCreated());

        criar(token, corpo("ESCORPIAO", "CASA", agora().plus(Duration.ofMinutes(30)), null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("dataHora"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("A data e a hora não podem estar no futuro"));
    }

    @Test
    void validacaoComAsMensagensDoApp() throws Exception {
        String token = ApoioDeTeste.cadastrarEEntrar(mvc);

        criar(token, """
                {"latitude":-40,"longitude":-46.6,"dataHora":"2026-11-10T10:00:00Z","observacao":"%s"}
                """.formatted("x".repeat(501)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[?(@.campo == 'tipoAnimal')].mensagem").value("Escolha o animal (ou “Não sei”)"))
                .andExpect(jsonPath("$.campos[?(@.campo == 'local')].mensagem").value("Escolha onde você viu"))
                .andExpect(jsonPath("$.campos[?(@.campo == 'latitude')].mensagem").value("A localização precisa ser no Brasil"))
                .andExpect(jsonPath("$.campos[?(@.campo == 'observacao')].mensagem").value("A observação pode ter até 500 caracteres"));

        criar(token, corpo("ESCORPIAO", "PRAIA", agora(), null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("local"));
    }

    @Test
    void cidadaoDeTesteJaTemTresAvistamentosDeExemplo() throws Exception {
        String resposta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cidadao@sospeconha.dev\",\"senha\":\"Teste@123\"}"))
                .andReturn().getResponse().getContentAsString();
        String token = "Bearer " + JsonPath.read(resposta, "$.token");

        mvc.perform(get("/api/avistamentos/meus").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.conteudo[0].observacao").value("Vi perto do ralo do banheiro"));
    }
}
