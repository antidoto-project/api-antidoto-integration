package br.com.fiap.antidoto.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaginaResponseTest {

    @Test
    void converteOPageDoSpringParaOFormatoDoContrato() {
        var page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 5);

        PaginaResponse<String> pagina = PaginaResponse.de(page);

        assertThat(pagina.conteudo()).containsExactly("a", "b");
        assertThat(pagina.pagina()).isEqualTo(1);
        assertThat(pagina.tamanho()).isEqualTo(2);
        assertThat(pagina.totalElementos()).isEqualTo(5);
        assertThat(pagina.totalPaginas()).isEqualTo(3);
    }
}
