package br.com.fiap.antidoto.controller;

import br.com.fiap.antidoto.dto.PaginaResponse;
import br.com.fiap.antidoto.dto.unidade.UnidadeProximaResponse;
import br.com.fiap.antidoto.dto.unidade.UnidadeResponse;
import br.com.fiap.antidoto.model.TipoAnimal;
import br.com.fiap.antidoto.service.UnidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Unidades: tudo público (o SOS funciona sem login, RN-G1). */
@RestController
@RequestMapping("/api/unidades")
@Tag(name = "Unidades")
@SecurityRequirements
public class UnidadeController {

    private final UnidadeService unidadeService;

    public UnidadeController(UnidadeService unidadeService) {
        this.unidadeService = unidadeService;
    }

    @GetMapping("/proximas")
    @Operation(summary = "Unidades mais próximas com soro compatível (público) — RN-S1…RN-S4")
    public ResponseEntity<List<UnidadeProximaResponse>> buscarProximas(
            @RequestParam
            @DecimalMin(value = "-34", message = "Latitude fora do Brasil")
            @DecimalMax(value = "6", message = "Latitude fora do Brasil")
            Double latitude,
            @RequestParam
            @DecimalMin(value = "-74", message = "Longitude fora do Brasil")
            @DecimalMax(value = "-34", message = "Longitude fora do Brasil")
            Double longitude,
            @RequestParam TipoAnimal tipoAnimal,
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "O limite vai de 1 a 10")
            @Max(value = 10, message = "O limite vai de 1 a 10")
            int limite
    ) {
        return ResponseEntity.ok(unidadeService.buscarProximas(latitude, longitude, tipoAnimal, limite));
    }

    @GetMapping
    @Operation(summary = "Lista paginada de unidades ativas (público)")
    public ResponseEntity<PaginaResponse<UnidadeResponse>> listar(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página começa em 0") int pagina,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "O tamanho vai de 1 a 50")
            @Max(value = 50, message = "O tamanho vai de 1 a 50")
            int tamanho
    ) {
        return ResponseEntity.ok(unidadeService.listar(pagina, tamanho));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe da unidade (público)")
    public ResponseEntity<UnidadeResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(unidadeService.buscar(id));
    }
}
