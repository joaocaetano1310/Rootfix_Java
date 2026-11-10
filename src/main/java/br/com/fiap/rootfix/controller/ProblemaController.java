package br.com.fiap.rootfix.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.rootfix.dto.ApoioResponse;
import br.com.fiap.rootfix.dto.EditarProblemaRequest;
import br.com.fiap.rootfix.dto.ObservacaoRequest;
import br.com.fiap.rootfix.dto.ObservacaoResponse;
import br.com.fiap.rootfix.dto.PaginaResponse;
import br.com.fiap.rootfix.dto.ProblemaResponse;
import br.com.fiap.rootfix.dto.PublicarProblemaRequest;
import br.com.fiap.rootfix.dto.TriagemResponse;
import br.com.fiap.rootfix.service.ProblemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@Tag(name = "Problemas", description = "Feed da comunidade, apoios, observações e adoção")
@SecurityRequirement(name = "bearerAuth")
public class ProblemaController {
    private final ProblemaService problemaService;

    public ProblemaController(ProblemaService problemaService) {
        this.problemaService = problemaService;
    }

    @PostMapping(value = "/comunidades/{comunidadeId}/problemas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Publica um problema com foto opcional (membros da comunidade)")
    public ProblemaResponse publicar(@PathVariable Long comunidadeId, @Valid @ModelAttribute PublicarProblemaRequest dados) {
        return problemaService.publicar(comunidadeId, dados);
    }

    @GetMapping("/comunidades/{comunidadeId}/problemas")
    @Operation(summary = "Feed paginado. ordem = recentes, apoiados ou gravidade (gravidade da IA e apoios)")
    public PaginaResponse<ProblemaResponse> feed(@PathVariable Long comunidadeId,
            @RequestParam(defaultValue = "recentes") String ordem,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return problemaService.feed(comunidadeId, ordem, page, size);
    }

    @GetMapping("/triagem")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Triagem da IA: problemas ainda não adotados das comunidades do gestor, do mais grave ao menos grave")
    public TriagemResponse triagem(@RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return problemaService.triagem(page, size);
    }

    @PostMapping("/problemas/{id}/reclassificar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Pede uma nova análise da IA para o problema (só o gestor da comunidade)")
    public void reclassificar(@PathVariable Long id) {
        problemaService.reclassificar(id);
    }

    @GetMapping("/comunidades/{comunidadeId}/supervisionados")
    @Operation(summary = "Problemas que o gestor adotou, com o status")
    public PaginaResponse<ProblemaResponse> supervisionados(@PathVariable Long comunidadeId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return problemaService.supervisionados(comunidadeId, page, size);
    }

    @GetMapping("/problemas/{id}")
    @Operation(summary = "Detalhe do problema")
    public ProblemaResponse buscar(@PathVariable Long id) {
        return problemaService.buscar(id);
    }

    @GetMapping("/problemas/{id}/foto")
    @Operation(summary = "Imagem do problema")
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        ProblemaService.Foto foto = problemaService.foto(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(foto.contentType())).body(foto.dados());
    }

    @PutMapping("/problemas/{id}")
    @Operation(summary = "Edita o problema (só o autor e só antes da adoção)")
    public ProblemaResponse editar(@PathVariable Long id, @Valid @RequestBody EditarProblemaRequest dados) {
        return problemaService.editar(id, dados);
    }

    @DeleteMapping("/problemas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui o problema (só o autor e só antes da adoção)")
    public void excluir(@PathVariable Long id) {
        problemaService.excluir(id);
    }

    @PostMapping("/problemas/{id}/apoios")
    @Operation(summary = "Apoia o problema; repetir a ação remove o apoio")
    public ApoioResponse apoiar(@PathVariable Long id) {
        return problemaService.apoiar(id);
    }

    @DeleteMapping("/problemas/{id}/apoios")
    @Operation(summary = "Retira o apoio")
    public ApoioResponse retirarApoio(@PathVariable Long id) {
        return problemaService.retirarApoio(id);
    }

    @PostMapping("/problemas/{id}/adotar")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Adota o problema (só o gestor da comunidade); status vira PENDENTE")
    public ProblemaResponse adotar(@PathVariable Long id) {
        return problemaService.adotar(id);
    }

    @GetMapping("/problemas/{id}/observacoes")
    @Operation(summary = "Observações do problema, com o nome de quem escreveu")
    public List<ObservacaoResponse> observacoes(@PathVariable Long id) {
        return problemaService.observacoes(id);
    }

    @PostMapping("/problemas/{id}/observacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona uma observação ao problema")
    public ObservacaoResponse observar(@PathVariable Long id, @Valid @RequestBody ObservacaoRequest dados) {
        return problemaService.observar(id, dados.texto());
    }

    @DeleteMapping("/observacoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a própria observação")
    public void excluirObservacao(@PathVariable Long id) {
        problemaService.excluirObservacao(id);
    }
}
