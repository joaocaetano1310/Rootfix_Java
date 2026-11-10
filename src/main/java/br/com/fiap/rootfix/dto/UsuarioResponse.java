package br.com.fiap.rootfix.dto;

import java.math.BigDecimal;

import br.com.fiap.rootfix.entity.Perfil;
import br.com.fiap.rootfix.entity.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        String organizacao,
        String endereco,
        BigDecimal latitude,
        BigDecimal longitude) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(),
                u.getOrganizacao(), u.getEndereco(), u.getLatitude(), u.getLongitude());
    }
}
