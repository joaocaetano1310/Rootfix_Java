package br.com.fiap.rootfix.controller;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.rootfix.dto.ComunidadeResponse;
import br.com.fiap.rootfix.dto.UsuarioResponse;
import br.com.fiap.rootfix.security.UsuarioLogado;
import br.com.fiap.rootfix.service.ComunidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/usuarios/me")
@Tag(name = "Minha conta", description = "Dados do usuário logado")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {
    private final UsuarioLogado usuarioLogado;
    private final ComunidadeService comunidadeService;

    public UsuarioController(UsuarioLogado usuarioLogado, ComunidadeService comunidadeService) {
        this.usuarioLogado = usuarioLogado;
        this.comunidadeService = comunidadeService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Dados da conta logada")
    public UsuarioResponse eu() {
        return UsuarioResponse.de(usuarioLogado.usuario());
    }

    @GetMapping("/comunidades")
    @Operation(summary = "Comunidades em que o morador participa ou que o gestor gerencia")
    public List<ComunidadeResponse> minhasComunidades() {
        return comunidadeService.minhas();
    }
}
