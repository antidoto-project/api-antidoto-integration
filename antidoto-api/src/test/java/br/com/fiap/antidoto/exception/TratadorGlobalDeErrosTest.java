package br.com.fiap.antidoto.exception;

import br.com.fiap.antidoto.model.TipoAnimal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O formato de erro do contrato (MVP-01 §7), com um controller só de teste. */
class TratadorGlobalDeErrosTest {

    record RequisicaoDeTeste(
            @NotBlank(message = "Informe o nome") String nome,
            @NotNull(message = "Informe o animal") TipoAnimal tipoAnimal
    ) {
    }

    @RestController
    static class ControllerDeTeste {

        @PostMapping("/teste/corpo")
        String corpo(@Valid @RequestBody RequisicaoDeTeste requisicao) {
            return "ok";
        }

        @GetMapping("/teste/parametro")
        String parametro(
                @RequestParam @DecimalMin(value = "-34", message = "Latitude fora do Brasil") Double latitude) {
            return "ok";
        }

        @GetMapping("/teste/regra")
        String regra() {
            throw new RegraDeNegocioException("horaPicada", "A hora da picada não pode estar no futuro");
        }

        @GetMapping("/teste/nao-encontrado")
        String naoEncontrado() {
            throw new RecursoNaoEncontradoException("Avistamento não encontrado.");
        }

        @GetMapping("/teste/conflito")
        String conflito() {
            throw new ConflitoException("E-mail já cadastrado.");
        }

        @GetMapping("/teste/falha")
        String falha() {
            throw new IllegalStateException("detalhe interno que não pode vazar");
        }
    }

    private static final Instant AGORA = Instant.parse("2026-11-10T22:15:00Z");

    private MockMvc mvc;

    @BeforeEach
    void montar() {
        mvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new TratadorGlobalDeErros(Clock.fixed(AGORA, ZoneOffset.UTC)))
                .build();
    }

    @Test
    void validacaoDoCorpoDevolveCadaCampoComAMensagem() throws Exception {
        mvc.perform(post("/teste/corpo").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Dados inválidos"))
                .andExpect(jsonPath("$.mensagem").value("Um ou mais campos são inválidos."))
                .andExpect(jsonPath("$.campos[?(@.campo == 'nome')].mensagem").value("Informe o nome"))
                .andExpect(jsonPath("$.campos[?(@.campo == 'tipoAnimal')].mensagem").value("Informe o animal"))
                .andExpect(jsonPath("$.caminho").value("/teste/corpo"))
                .andExpect(jsonPath("$.timestamp").value("2026-11-10T22:15:00Z"));
    }

    @Test
    void enumDesconhecidoApontaOCampo() throws Exception {
        mvc.perform(post("/teste/corpo").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"tipoAnimal\":\"PEIXE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("tipoAnimal"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("Valor inválido"));
    }

    @Test
    void jsonMalformadoViraMensagemGeral() throws Exception {
        mvc.perform(post("/teste/corpo").contentType(MediaType.APPLICATION_JSON).content("{nome"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O corpo da requisição não é um JSON válido."))
                .andExpect(jsonPath("$.campos").doesNotExist());
    }

    @Test
    void parametroDaUrlInvalidoOuDeTipoErrado() throws Exception {
        mvc.perform(get("/teste/parametro").param("latitude", "-40"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("latitude"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("Latitude fora do Brasil"));

        mvc.perform(get("/teste/parametro").param("latitude", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("latitude"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("Valor inválido"));

        mvc.perform(get("/teste/parametro"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("latitude"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("Campo obrigatório"));
    }

    @Test
    void regraDeNegocioVaiParaOCampo() throws Exception {
        mvc.perform(get("/teste/regra"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("horaPicada"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("A hora da picada não pode estar no futuro"));
    }

    @Test
    void naoEncontradoEConflito() throws Exception {
        mvc.perform(get("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Não encontrado"))
                .andExpect(jsonPath("$.mensagem").value("Avistamento não encontrado."));

        mvc.perform(get("/teste/conflito"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Conflito"))
                .andExpect(jsonPath("$.mensagem").value("E-mail já cadastrado."));
    }

    @Test
    void erroInesperadoNaoVazaODetalhe() throws Exception {
        mvc.perform(get("/teste/falha"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.erro").value("Erro interno"))
                .andExpect(jsonPath("$.mensagem").value("Erro inesperado. Tente novamente mais tarde."));
    }
}
