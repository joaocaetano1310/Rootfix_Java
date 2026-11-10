package br.com.fiap.rootfix.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PaginaResponse<T>(List<T> content, PaginaInfo page) {
    public record PaginaInfo(int size, int number, long totalElements, int totalPages) {
    }

    public static <T> PaginaResponse<T> de(Page<T> pagina) {
        return new PaginaResponse<>(pagina.getContent(), new PaginaInfo(pagina.getSize(),
                pagina.getNumber(), pagina.getTotalElements(), pagina.getTotalPages()));
    }

    public static <T> PaginaResponse<T> de(List<T> todos, int numero, int tamanho) {
        int inicio = Math.min(numero * tamanho, todos.size());
        int fim = Math.min(inicio + tamanho, todos.size());
        int totalPaginas = (int) Math.ceil(todos.size() / (double) tamanho);
        return new PaginaResponse<>(todos.subList(inicio, fim),
                new PaginaInfo(tamanho, numero, todos.size(), totalPaginas));
    }
}
