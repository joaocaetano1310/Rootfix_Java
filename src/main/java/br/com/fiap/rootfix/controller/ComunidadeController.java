package br.com.fiap.rootfix.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.rootfix.dto.ComunidadeRequest;
import br.com.fiap.rootfix.dto.ComunidadeResponse;
import br.com.fiap.rootfix.dto.PaginaResponse;
import br.com.fiap.rootfix.service.ComunidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/comunidades")
@Tag(name = "Comunidades", description = "Comunidades criadas pelos gestores e frequentadas pelos moradores")
@SecurityRequirement(name = "bearerAuth")
public class ComunidadeController {
    private final ComunidadeService comunidadeService;

    public ComunidadeController(ComunidadeService comunidadeService) {
        this.comunidadeService = comunidadeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Cria mais uma comunidade (gestor)")
    public ComunidadeResponse criar(@Valid @RequestBody ComunidadeRequest dados) {
        return comunidadeService.criar(dados);
    }

    @GetMapping("/proximas")
    @PreAuthorize("hasRole('MORADOR')")
    @Operation(summary = "Lista as comunidades a até 3 km do endereço do morador, da mais perto para a mais longe")
    public PaginaResponse<ComunidadeResponse> proximas(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return comunidadeService.proximas(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de uma comunidade, com o gestor responsável")
    public ComunidadeResponse buscar(@PathVariable Long id) {
        return comunidadeService.buscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Edita a comunidade (só o gestor que a criou)")
    public ComunidadeResponse atualizar(@PathVariable Long id, @Valid @RequestBody ComunidadeRequest dados) {
        return comunidadeService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Exclui a comunidade, se ainda não tiver problemas publicados (só o gestor que a criou)")
    public void excluir(@PathVariable Long id) {
        comunidadeService.excluir(id);
    }

    @PostMapping("/{id}/membros")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MORADOR')")
    @Operation(summary = "Entra na comunidade (o endereço do morador precisa estar a até 3 km)")
    public ComunidadeResponse entrar(@PathVariable Long id) {
        return comunidadeService.entrar(id);
    }

    @DeleteMapping("/{id}/membros")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MORADOR')")
    @Operation(summary = "Sai da comunidade")
    public void sair(@PathVariable Long id) {
        comunidadeService.sair(id);
    }
}
