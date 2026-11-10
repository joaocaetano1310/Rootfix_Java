package br.com.fiap.rootfix.dto;

import java.time.LocalDateTime;

import br.com.fiap.rootfix.entity.Comunicado;

public record ComunicadoResponse(Long id, Long comunidadeId, String titulo, String texto, boolean destacado,
        ResumoUsuario autor, LocalDateTime criadoEm, boolean souAutor) {
    public static ComunicadoResponse de(Comunicado comunicado, Long usuarioLogadoId) {
        return new ComunicadoResponse(comunicado.getId(), comunicado.getComunidade().getId(), comunicado.getTitulo(),
                comunicado.getTexto(), comunicado.isDestacado(), ResumoUsuario.de(comunicado.getAutor()),
                comunicado.getCriadoEm(), comunicado.getAutor().getId().equals(usuarioLogadoId));
    }
}
