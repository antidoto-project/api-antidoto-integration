package br.com.fiap.antidoto;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Atalhos dos testes de integração: criar uma conta nova e pegar o token. */
public final class ApoioDeTeste {

    private ApoioDeTeste() {
    }

    /** E-mail que nenhum outro teste usa (o banco H2 é o mesmo para todos). */
    public static String emailNovo() {
        return "teste-" + UUID.randomUUID() + "@antidoto.dev";
    }

    /** Cadastra uma conta nova e devolve o "Bearer ..." pronto para o cabeçalho. */
    public static String cadastrarEEntrar(MockMvc mvc) throws Exception {
        String corpo = """
                {"nome":"Pessoa de Teste","email":"%s","senha":"Teste123","telefone":null}
                """.formatted(emailNovo());
        String resposta = mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.token");
    }
}
