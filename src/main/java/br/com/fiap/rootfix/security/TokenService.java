package br.com.fiap.rootfix.security;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.fiap.rootfix.entity.Usuario;

@Service
public class TokenService {
    public record Token(String valor, long expiraEmSegundos) {
    }

    private final JwtEncoder jwtEncoder;
    private final JwtProperties propriedades;

    public TokenService(JwtEncoder jwtEncoder, JwtProperties propriedades) {
        this.jwtEncoder = jwtEncoder;
        this.propriedades = propriedades;
    }

    public Token gerar(Usuario usuario) {
        Instant agora = Instant.now();
        long segundos = propriedades.expiracaoMinutos() * 60;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propriedades.issuer())
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(segundos))
                .claim("perfil", usuario.getPerfil().name())
                .claim("nome", usuario.getNome())
                .claim("email", usuario.getEmail())
                .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new Token(valor, segundos);
    }
}
