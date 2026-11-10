package br.com.fiap.rootfix.dto;

import java.time.LocalDateTime;

import br.com.fiap.rootfix.entity.Observacao;

public record ObservacaoResponse(Long id, String texto, ResumoUsuario autor, LocalDateTime criadaEm, boolean souAutor) {
    public static ObservacaoResponse de(Observacao observacao, Long usuarioLogadoId) {
        return new ObservacaoResponse(observacao.getId(), observacao.getTexto(), ResumoUsuario.de(observacao.getAutor()),
                observacao.getCriadaEm(), observacao.getAutor().getId().equals(usuarioLogadoId));
    }
}
