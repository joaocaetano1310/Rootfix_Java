package br.com.fiap.rootfix.dto;

import br.com.fiap.rootfix.entity.Comunidade;

public record ResumoComunidade(Long id, String nome) {
    public static ResumoComunidade de(Comunidade comunidade) {
        return new ResumoComunidade(comunidade.getId(), comunidade.getNome());
    }
}
