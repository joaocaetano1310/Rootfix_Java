package br.com.fiap.rootfix.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.rootfix.entity.Gravidade;
import br.com.fiap.rootfix.entity.Problema;
import br.com.fiap.rootfix.entity.StatusProblema;

public record ProblemaResponse(
        Long id,
        String titulo,
        String descricao,
        BigDecimal latitude,
        BigDecimal longitude,
        ResumoUsuario autor,
        ResumoComunidade comunidade,
        CategoriaResponse categoria,
        boolean adotado,
        LocalDateTime adotadoEm,
        StatusProblema status,
        String prioridade,
        Gravidade gravidadeIa,
        String justificativaIa,
        long totalApoios,
        boolean apoiadoPorMim,
        long totalObservacoes,
        boolean temFoto,
        boolean souAutor,
        boolean souGestor,
        LocalDateTime criadoEm) {
    public static ProblemaResponse de(Problema p, long totalApoios, boolean apoiadoPorMim, long totalObservacoes,
            boolean temFoto, boolean souAutor, boolean souGestor) {
        return new ProblemaResponse(p.getId(), p.getTitulo(), p.getDescricao(), p.getLatitude(), p.getLongitude(),
                ResumoUsuario.de(p.getAutor()), ResumoComunidade.de(p.getComunidade()),
                p.getCategoria() == null ? null : CategoriaResponse.de(p.getCategoria()),
                p.isAdotado(), p.getAdotadoEm(), p.getStatus(), p.getPrioridade(), p.getGravidadeIa(),
                p.getJustificativaIa(), totalApoios, apoiadoPorMim, totalObservacoes, temFoto, souAutor, souGestor,
                p.getCriadoEm());
    }
}
