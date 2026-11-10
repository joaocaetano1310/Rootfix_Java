package br.com.fiap.rootfix.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.rootfix.dto.ComunidadeRequest;
import br.com.fiap.rootfix.dto.ComunidadeResponse;
import br.com.fiap.rootfix.dto.PaginaResponse;
import br.com.fiap.rootfix.entity.Comunidade;
import br.com.fiap.rootfix.entity.Participacao;
import br.com.fiap.rootfix.entity.Perfil;
import br.com.fiap.rootfix.entity.Usuario;
import br.com.fiap.rootfix.exception.AcessoNegadoException;
import br.com.fiap.rootfix.exception.ConflitoException;
import br.com.fiap.rootfix.exception.NaoEncontradoException;
import br.com.fiap.rootfix.exception.RegraNegocioException;
import br.com.fiap.rootfix.repository.ComunidadeRepository;
import br.com.fiap.rootfix.repository.ParticipacaoRepository;
import br.com.fiap.rootfix.repository.ProblemaRepository;
import br.com.fiap.rootfix.security.UsuarioLogado;

@Service
public class ComunidadeService {
    public static final double RAIO_MAXIMO_KM = 3.0;

    private final ComunidadeRepository comunidadeRepository;
    private final ParticipacaoRepository participacaoRepository;
    private final ProblemaRepository problemaRepository;
    private final UsuarioLogado usuarioLogado;

    public ComunidadeService(ComunidadeRepository comunidadeRepository, ParticipacaoRepository participacaoRepository,
            ProblemaRepository problemaRepository, UsuarioLogado usuarioLogado) {
        this.comunidadeRepository = comunidadeRepository;
        this.participacaoRepository = participacaoRepository;
        this.problemaRepository = problemaRepository;
        this.usuarioLogado = usuarioLogado;
    }

    @Transactional
    public ComunidadeResponse criar(ComunidadeRequest dados) {
        Usuario gestor = usuarioLogado.usuario();
        return paraResposta(criarPara(gestor, dados), gestor);
    }

    @Transactional
    public Comunidade criarPara(Usuario gestor, ComunidadeRequest dados) {
        Comunidade comunidade = new Comunidade();
        comunidade.setGestor(gestor);
        preencher(comunidade, dados);
        return comunidadeRepository.save(comunidade);
    }

    @Transactional(readOnly = true)
    public ComunidadeResponse buscar(Long id) {
        return paraResposta(obter(id), usuarioLogado.usuario());
    }

    @Transactional
    public ComunidadeResponse atualizar(Long id, ComunidadeRequest dados) {
        Usuario eu = usuarioLogado.usuario();
        Comunidade comunidade = obter(id);
        exigirGestorDono(comunidade, eu);
        preencher(comunidade, dados);
        return paraResposta(comunidade, eu);
    }

    @Transactional
    public void excluir(Long id) {
        Comunidade comunidade = obter(id);
        exigirGestorDono(comunidade, usuarioLogado.usuario());
        if (problemaRepository.existsByComunidadeId(id)) {
            throw new ConflitoException("Esta comunidade já tem problemas publicados e não pode ser excluída.");
        }
        comunidadeRepository.delete(comunidade);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ComunidadeResponse> proximas(int pagina, int tamanho) {
        Usuario eu = usuarioLogado.usuario();
        BigDecimal deltaLatitude = Distancia.grausDeLatitude(RAIO_MAXIMO_KM);
        BigDecimal deltaLongitude = Distancia.grausDeLongitude(RAIO_MAXIMO_KM, eu.getLatitude());

        List<ComunidadeResponse> dentroDoRaio = comunidadeRepository
                .findByLatitudeBetweenAndLongitudeBetween(
                        eu.getLatitude().subtract(deltaLatitude), eu.getLatitude().add(deltaLatitude),
                        eu.getLongitude().subtract(deltaLongitude), eu.getLongitude().add(deltaLongitude))
                .stream()
                .filter(c -> distanciaKm(eu, c) <= RAIO_MAXIMO_KM)
                .map(c -> paraResposta(c, eu))
                .sorted(Comparator.comparing(ComunidadeResponse::distanciaKm))
                .toList();

        return PaginaResponse.de(dentroDoRaio, pagina, tamanho);
    }

    @Transactional(readOnly = true)
    public List<ComunidadeResponse> minhas() {
        Usuario eu = usuarioLogado.usuario();
        List<Comunidade> comunidades = eu.getPerfil() == Perfil.GESTOR
                ? comunidadeRepository.findByGestorIdOrderByNomeAsc(eu.getId())
                : participacaoRepository.findByUsuarioIdOrderByEntrouEmDesc(eu.getId()).stream()
                        .map(Participacao::getComunidade)
                        .toList();
        return comunidades.stream().map(c -> paraResposta(c, eu)).toList();
    }

    @Transactional
    public ComunidadeResponse entrar(Long id) {
        Usuario eu = usuarioLogado.usuario();
        Comunidade comunidade = obter(id);

        if (participacaoRepository.existsByUsuarioIdAndComunidadeId(eu.getId(), id)) {
            throw new ConflitoException("Você já participa desta comunidade.");
        }
        double distancia = distanciaKm(eu, comunidade);
        if (distancia > RAIO_MAXIMO_KM) {
            throw new RegraNegocioException(String.format(Locale.of("pt", "BR"),
                    "Esta comunidade fica a %.1f km do seu endereço. Só é possível entrar em comunidades a até 3 km.",
                    distancia));
        }

        participacaoRepository.save(new Participacao(eu, comunidade));
        return paraResposta(comunidade, eu);
    }

    @Transactional
    public void sair(Long id) {
        Usuario eu = usuarioLogado.usuario();
        obter(id);
        Participacao participacao = participacaoRepository.findByUsuarioIdAndComunidadeId(eu.getId(), id)
                .orElseThrow(() -> new NaoEncontradoException("Você não participa desta comunidade."));
        participacaoRepository.delete(participacao);
    }

    public Comunidade obter(Long id) {
        return comunidadeRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Comunidade não encontrada."));
    }

    private void exigirGestorDono(Comunidade comunidade, Usuario usuario) {
        if (!comunidade.getGestor().getId().equals(usuario.getId())) {
            throw new AcessoNegadoException("Só o gestor que criou esta comunidade pode alterá-la.");
        }
    }

    private void preencher(Comunidade comunidade, ComunidadeRequest dados) {
        comunidade.setNome(dados.nome().trim());
        comunidade.setDescricao(dados.descricao() == null || dados.descricao().isBlank() ? null : dados.descricao().trim());
        comunidade.setLatitude(Distancia.coordenada(dados.latitude()));
        comunidade.setLongitude(Distancia.coordenada(dados.longitude()));
    }

    private double distanciaKm(Usuario usuario, Comunidade comunidade) {
        return Distancia.km(usuario.getLatitude(), usuario.getLongitude(), comunidade.getLatitude(), comunidade.getLongitude());
    }

    private ComunidadeResponse paraResposta(Comunidade comunidade, Usuario eu) {
        boolean souGestor = comunidade.getGestor().getId().equals(eu.getId());
        boolean participo = eu.getPerfil() == Perfil.MORADOR
                && participacaoRepository.existsByUsuarioIdAndComunidadeId(eu.getId(), comunidade.getId());
        Double distancia = eu.getLatitude() == null || eu.getLongitude() == null
                ? null
                : Distancia.arredondar(distanciaKm(eu, comunidade));
        return ComunidadeResponse.de(comunidade, participacaoRepository.countByComunidadeId(comunidade.getId()),
                distancia, participo, souGestor);
    }
}
