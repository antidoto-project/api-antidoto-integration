package br.com.fiap.antidoto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Página inicial (static/index.html): pública, para abrir o endereço do Web App sem login.
 * Não confere o <h1>, que é o texto que muda no vídeo da entrega.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PaginaInicialTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void raizEncaminhaParaAPaginaInicialSemLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void paginaInicialAbreSemLoginComOLinkDoSwagger() throws Exception {
        // O arquivo é UTF-8 (<meta charset>); o MockMvc lê em ISO-8859-1 se não for avisado
        String html = mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("<title>Antídoto</title>", "href=\"/swagger-ui.html\"");
    }
}
