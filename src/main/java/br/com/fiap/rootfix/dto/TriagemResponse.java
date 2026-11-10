package br.com.fiap.rootfix.dto;

public record TriagemResponse(
        long criticos,
        long altos,
        long medios,
        long baixos,
        long semClassificacao,
        PaginaResponse<ProblemaResponse> problemas) {
}
