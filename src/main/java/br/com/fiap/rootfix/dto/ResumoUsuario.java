package br.com.fiap.rootfix.dto;

import br.com.fiap.rootfix.entity.Usuario;

public record ResumoUsuario(Long id, String nome) {
    public static ResumoUsuario de(Usuario usuario) {
        return new ResumoUsuario(usuario.getId(), usuario.getNome());
    }
}
