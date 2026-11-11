package br.com.fiap.antidoto.controller;

import br.com.fiap.antidoto.dto.usuario.AuthResponse;
import br.com.fiap.antidoto.dto.usuario.CadastroRequest;
import br.com.fiap.antidoto.dto.usuario.LoginRequest;
import br.com.fiap.antidoto.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastro de cidadão (já devolve o token)")
    public ResponseEntity<AuthResponse> cadastrar(@Valid @RequestBody CadastroRequest requisicao) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.cadastrar(requisicao));
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<AuthResponse> entrar(@Valid @RequestBody LoginRequest requisicao) {
        return ResponseEntity.ok(authService.entrar(requisicao));
    }
}
