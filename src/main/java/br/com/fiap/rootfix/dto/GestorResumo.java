package br.com.fiap.rootfix.dto;

import br.com.fiap.rootfix.entity.Usuario;

public record GestorResumo(Long id, String nome, String organizacao) {
    public static GestorResumo de(Usuario gestor) {
        return new GestorResumo(gestor.getId(), gestor.getNome(), gestor.getOrganizacao());
    }
}
