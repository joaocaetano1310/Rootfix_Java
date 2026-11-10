package br.com.fiap.rootfix.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.rootfix.dto.AuthResponse;
import br.com.fiap.rootfix.dto.LoginRequest;
import br.com.fiap.rootfix.dto.RegistroGestorRequest;
import br.com.fiap.rootfix.dto.RegistroMoradorRequest;
import br.com.fiap.rootfix.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Cadastro e login com JWT")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro/morador")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma conta de morador com endereço e coordenadas")
    public AuthResponse registrarMorador(@Valid @RequestBody RegistroMoradorRequest dados) {
        return authService.registrarMorador(dados);
    }

    @PostMapping("/registro/gestor")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma conta de gestor junto com a primeira comunidade")
    public AuthResponse registrarGestor(@Valid @RequestBody RegistroGestorRequest dados) {
        return authService.registrarGestor(dados);
    }

    @PostMapping("/login")
    @Operation(summary = "Entra com e-mail e senha e devolve o token JWT")
    public AuthResponse entrar(@Valid @RequestBody LoginRequest dados) {
        return authService.entrar(dados);
    }
}
