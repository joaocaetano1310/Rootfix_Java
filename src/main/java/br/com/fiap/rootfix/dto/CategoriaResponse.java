package br.com.fiap.rootfix.dto;

import br.com.fiap.rootfix.entity.Categoria;

public record CategoriaResponse(Long id, String nome) {
    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
