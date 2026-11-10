package br.com.fiap.rootfix.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.rootfix.dto.ComunicadoRequest;
import br.com.fiap.rootfix.dto.ComunicadoResponse;
import br.com.fiap.rootfix.dto.PaginaResponse;
import br.com.fiap.rootfix.service.ComunicadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@Tag(name = "Comunicados", description = "Avisos que o gestor publica para a comunidade, com opção de destaque")
@SecurityRequirement(name = "bearerAuth")
public class ComunicadoController {
    private final ComunicadoService comunicadoService;

    public ComunicadoController(ComunicadoService comunicadoService) {
        this.comunicadoService = comunicadoService;
    }

    @GetMapping("/comunidades/{id}/comunicados")
    @Operation(summary = "Comunicados da comunidade: os destacados primeiro, depois os mais recentes")
    public PaginaResponse<ComunicadoResponse> listar(@PathVariable Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return comunicadoService.listar(id, page, size);
    }

    @PostMapping("/comunidades/{id}/comunicados")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Publica um comunicado na comunidade (só o gestor dela). Destacado avisa os membros com prioridade alta")
    public ComunicadoResponse publicar(@PathVariable Long id, @Valid @RequestBody ComunicadoRequest dados) {
        return comunicadoService.publicar(id, dados);
    }

    @PutMapping("/comunicados/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Edita o comunicado ou muda o destaque (só o gestor da comunidade)")
    public ComunicadoResponse editar(@PathVariable Long id, @Valid @RequestBody ComunicadoRequest dados) {
        return comunicadoService.editar(id, dados);
    }

    @DeleteMapping("/comunicados/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Exclui o comunicado (só o gestor da comunidade)")
    public void excluir(@PathVariable Long id) {
        comunicadoService.excluir(id);
    }
}
