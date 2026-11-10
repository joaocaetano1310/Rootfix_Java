package br.com.fiap.rootfix.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import br.com.fiap.rootfix.entity.Usuario;
import br.com.fiap.rootfix.exception.CredenciaisInvalidasException;
import br.com.fiap.rootfix.repository.UsuarioRepository;

@Component
public class UsuarioLogado {
    private final UsuarioRepository usuarioRepository;

    public UsuarioLogado(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Long id() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao instanceof JwtAuthenticationToken token) {
            return Long.valueOf(token.getToken().getSubject());
        }
        throw new CredenciaisInvalidasException("Faça login para continuar.");
    }

    public Usuario usuario() {
        return usuarioRepository.findById(id())
                .orElseThrow(() -> new CredenciaisInvalidasException("Sua conta não foi encontrada. Faça login de novo."));
    }
}
