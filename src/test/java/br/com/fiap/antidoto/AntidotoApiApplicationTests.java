package br.com.fiap.antidoto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AntidotoApiApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void sobeComOFlywayEOHibernateConferindoAsEntidades() {
        // Se o SQL do Flyway e as entidades não baterem, o contexto nem sobe (ddl-auto=validate)
    }

    @Test
    void saudeRespondeUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void swaggerPublicado() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Antídoto — API Java"));
    }

    @Test
    void rotaInexistenteRespondeNoFormatoDoContrato() throws Exception {
        mvc.perform(get("/api/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Não encontrado"))
                .andExpect(jsonPath("$.mensagem").value("Recurso não encontrado."))
                .andExpect(jsonPath("$.caminho").value("/api/nao-existe"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.campos").doesNotExist());
    }
}
