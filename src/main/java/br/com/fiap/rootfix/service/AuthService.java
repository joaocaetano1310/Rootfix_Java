package br.com.fiap.rootfix.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.rootfix.dto.AuthResponse;
import br.com.fiap.rootfix.dto.LoginRequest;
import br.com.fiap.rootfix.dto.RegistroGestorRequest;
import br.com.fiap.rootfix.dto.RegistroMoradorRequest;
import br.com.fiap.rootfix.dto.UsuarioResponse;
import br.com.fiap.rootfix.entity.Perfil;
import br.com.fiap.rootfix.entity.Usuario;
import br.com.fiap.rootfix.exception.ConflitoException;
import br.com.fiap.rootfix.exception.CredenciaisInvalidasException;
import br.com.fiap.rootfix.repository.UsuarioRepository;
import br.com.fiap.rootfix.security.TokenService;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final ComunidadeService comunidadeService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            TokenService tokenService, ComunidadeService comunidadeService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.comunidadeService = comunidadeService;
    }

    @Transactional
    public AuthResponse registrarMorador(RegistroMoradorRequest dados) {
        Usuario morador = novoUsuario(dados.nome(), dados.email(), dados.senha(), Perfil.MORADOR);
        morador.setEndereco(dados.endereco().trim());
        morador.setLatitude(Distancia.coordenada(dados.latitude()));
        morador.setLongitude(Distancia.coordenada(dados.longitude()));
        usuarioRepository.save(morador);
        return autenticado(morador);
    }

    @Transactional
    public AuthResponse registrarGestor(RegistroGestorRequest dados) {
        Usuario gestor = novoUsuario(dados.nome(), dados.email(), dados.senha(), Perfil.GESTOR);
        gestor.setOrganizacao(dados.organizacao().trim());
        usuarioRepository.save(gestor);
        comunidadeService.criarPara(gestor, dados.comunidade());
        return autenticado(gestor);
    }

    @Transactional(readOnly = true)
    public AuthResponse entrar(LoginRequest dados) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dados.email().trim())
                .filter(u -> passwordEncoder.matches(dados.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos."));
        return autenticado(usuario);
    }

    private Usuario novoUsuario(String nome, String email, String senha, Perfil perfil) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ConflitoException("Já existe uma conta com este e-mail.");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        return usuario;
    }

    private AuthResponse autenticado(Usuario usuario) {
        TokenService.Token token = tokenService.gerar(usuario);
        return new AuthResponse(token.valor(), "Bearer", token.expiraEmSegundos(), UsuarioResponse.de(usuario));
    }
}
