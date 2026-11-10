package br.com.fiap.rootfix.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.rootfix.dto.NotificacaoResponse;
import br.com.fiap.rootfix.dto.PaginaResponse;
import br.com.fiap.rootfix.service.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/notificacoes")
@Tag(name = "Notificações", description = "Avisos com prioridade alta ou baixa")
@SecurityRequirement(name = "bearerAuth")
public class NotificacaoController {
    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    @Operation(summary = "Minhas notificações, da mais nova para a mais antiga")
    public PaginaResponse<NotificacaoResponse> minhas(@RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int size) {
        return notificacaoService.minhas(page, size);
    }

    @PatchMapping("/{id}/lida")
    @Operation(summary = "Marca a notificação como lida")
    public NotificacaoResponse marcarComoLida(@PathVariable Long id) {
        return notificacaoService.marcarComoLida(id);
    }
}
