package br.com.fiap.antidoto.controller;

import br.com.fiap.antidoto.dto.PaginaResponse;
import br.com.fiap.antidoto.dto.avistamento.AvistamentoRequest;
import br.com.fiap.antidoto.dto.avistamento.AvistamentoResponse;
import br.com.fiap.antidoto.security.UsuarioAutenticado;
import br.com.fiap.antidoto.service.AvistamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Avistamentos: o CRUD do app (logado; só os do próprio usuário, RN-G5). */
@RestController
@RequestMapping("/api/avistamentos")
@Tag(name = "Avistamentos")
public class AvistamentoController {

    private final AvistamentoService avistamentoService;

    public AvistamentoController(AvistamentoService avistamentoService) {
        this.avistamentoService = avistamentoService;
    }

    @PostMapping
    @Operation(summary = "Registra avistamento (logado)")
    public ResponseEntity<AvistamentoResponse> criar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody AvistamentoRequest requisicao) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(avistamentoService.criar(usuario.id(), requisicao));
    }

    @GetMapping("/meus")
    @Operation(summary = "Meus avistamentos (mais recentes primeiro)")
    public ResponseEntity<PaginaResponse<AvistamentoResponse>> listarMeus(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página começa em 0") int pagina,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "O tamanho vai de 1 a 50")
            @Max(value = 50, message = "O tamanho vai de 1 a 50")
            int tamanho) {
        return ResponseEntity.ok(avistamentoService.listarMeus(usuario.id(), pagina, tamanho));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe (somente o dono — RN-G5)")
    public ResponseEntity<AvistamentoResponse> buscar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {
        return ResponseEntity.ok(avistamentoService.buscar(usuario.id(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita (somente o dono)")
    public ResponseEntity<AvistamentoResponse> atualizar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @Valid @RequestBody AvistamentoRequest requisicao) {
        return ResponseEntity.ok(avistamentoService.atualizar(usuario.id(), id, requisicao));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui (somente o dono)")
    public ResponseEntity<Void> excluir(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {
        avistamentoService.excluir(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
