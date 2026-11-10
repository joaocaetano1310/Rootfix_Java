package br.com.fiap.rootfix.config;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.rootfix.dto.ComunidadeRequest;
import br.com.fiap.rootfix.entity.Comunidade;
import br.com.fiap.rootfix.entity.Participacao;
import br.com.fiap.rootfix.entity.Perfil;
import br.com.fiap.rootfix.entity.Usuario;
import br.com.fiap.rootfix.repository.ComunidadeRepository;
import br.com.fiap.rootfix.repository.ParticipacaoRepository;
import br.com.fiap.rootfix.repository.UsuarioRepository;
import br.com.fiap.rootfix.service.ComunidadeService;

@Component
public class DadosIniciais implements ApplicationRunner {
    public static final String SENHA_DE_TESTE = "Rootfix@2026";
    public static final String EMAIL_GESTOR = "gestor@rootfix.com";
    public static final String EMAIL_MORADOR = "morador@rootfix.com";

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    private final boolean ativo;
    private final UsuarioRepository usuarioRepository;
    private final ComunidadeRepository comunidadeRepository;
    private final ParticipacaoRepository participacaoRepository;
    private final ComunidadeService comunidadeService;
    private final PasswordEncoder passwordEncoder;

    public DadosIniciais(@Value("${rootfix.dados-iniciais:true}") boolean ativo, UsuarioRepository usuarioRepository,
            ComunidadeRepository comunidadeRepository, ParticipacaoRepository participacaoRepository,
            ComunidadeService comunidadeService, PasswordEncoder passwordEncoder) {
        this.ativo = ativo;
        this.usuarioRepository = usuarioRepository;
        this.comunidadeRepository = comunidadeRepository;
        this.participacaoRepository = participacaoRepository;
        this.comunidadeService = comunidadeService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!ativo) {
            return;
        }

        Usuario gestor = usuarioRepository.findByEmailIgnoreCase(EMAIL_GESTOR).orElseGet(() -> {
            Usuario novo = novoUsuario("Marta Ribeiro", EMAIL_GESTOR, Perfil.GESTOR);
            novo.setOrganizacao("Associação de Moradores da Bela Vista");
            return usuarioRepository.save(novo);
        });

        Comunidade belaVista;
        if (comunidadeRepository.findByGestorIdOrderByNomeAsc(gestor.getId()).isEmpty()) {
            belaVista = comunidadeService.criarPara(gestor, new ComunidadeRequest("Bela Vista - Av. Paulista",
                    "Quadras em volta da Av. Paulista, entre a Consolação e o Paraíso.",
                    new BigDecimal("-23.5640000"), new BigDecimal("-46.6527000")));
            comunidadeService.criarPara(gestor, new ComunidadeRequest("Mooca - Rua da Mooca",
                    "Ruas residenciais da Mooca, perto do Juventus.",
                    new BigDecimal("-23.5578000"), new BigDecimal("-46.5999000")));
        } else {
            belaVista = comunidadeRepository.findByGestorIdOrderByNomeAsc(gestor.getId()).get(0);
        }

        Usuario morador = usuarioRepository.findByEmailIgnoreCase(EMAIL_MORADOR).orElseGet(() -> {
            Usuario novo = novoUsuario("João Silva", EMAIL_MORADOR, Perfil.MORADOR);
            novo.setEndereco("Rua Frei Caneca, 1000 - Bela Vista, São Paulo");
            novo.setLatitude(new BigDecimal("-23.5578000"));
            novo.setLongitude(new BigDecimal("-46.6556000"));
            return usuarioRepository.save(novo);
        });

        if (!participacaoRepository.existsByUsuarioIdAndComunidadeId(morador.getId(), belaVista.getId())) {
            participacaoRepository.save(new Participacao(morador, belaVista));
        }

        log.info("Contas de teste prontas: {} e {} (senha {})", EMAIL_GESTOR, EMAIL_MORADOR, SENHA_DE_TESTE);
    }

    private Usuario novoUsuario(String nome, String email, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_DE_TESTE));
        usuario.setPerfil(perfil);
        return usuario;
    }
}
