package br.com.fiap.rootfix.dto;

public record AuthResponse(String token, String tipo, long expiraEmSegundos, UsuarioResponse usuario) {
}
