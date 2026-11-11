package br.com.fiap.antidoto.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Lista paginada no formato do contrato (MVP-01 §6.5). Nunca devolver o Page do
 * Spring direto: os nomes dos campos são os do app.
 */
public record PaginaResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas
) {

    public static <T> PaginaResponse<T> de(Page<T> pagina) {
        return new PaginaResponse<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }
}
