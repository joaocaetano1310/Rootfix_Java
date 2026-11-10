package br.com.fiap.rootfix.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.fiap.rootfix.dto.ApoioResponse;
import br.com.fiap.rootfix.dto.EditarProblemaRequest;
import br.com.fiap.rootfix.dto.ObservacaoResponse;
import br.com.fiap.rootfix.dto.PaginaResponse;
import br.com.fiap.rootfix.dto.ProblemaResponse;
import br.com.fiap.rootfix.dto.PublicarProblemaRequest;
import br.com.fiap.rootfix.dto.TriagemResponse;
import br.com.fiap.rootfix.entity.Apoio;
import br.com.fiap.rootfix.entity.Comunidade;
import br.com.fiap.rootfix.entity.FotoProblema;
import br.com.fiap.rootfix.entity.Gravidade;
import br.com.fiap.rootfix.entity.Observacao;
import br.com.fiap.rootfix.entity.Perfil;
import br.com.fiap.rootfix.entity.Problema;
import br.com.fiap.rootfix.entity.StatusProblema;
import br.com.fiap.rootfix.entity.Usuario;
import br.com.fiap.rootfix.exception.AcessoNegadoException;
import br.com.fiap.rootfix.exception.ConflitoException;
import br.com.fiap.rootfix.exception.NaoEncontradoException;
import br.com.fiap.rootfix.exception.RegraNegocioException;
import br.com.fiap.rootfix.messaging.ObservacaoAdicionadaEvento;
import br.com.fiap.rootfix.messaging.ProblemaAdotadoEvento;
import br.com.fiap.rootfix.messaging.ProblemaPublicadoEvento;
import br.com.fiap.rootfix.repository.ApoioRepository;
import br.com.fiap.rootfix.repository.ComunidadeRepository;
import br.com.fiap.rootfix.repository.FotoProblemaRepository;
import br.com.fiap.rootfix.repository.ObservacaoRepository;
import br.com.fiap.rootfix.repository.ParticipacaoRepository;
import br.com.fiap.rootfix.repository.ProblemaRepository;
import br.com.fiap.rootfix.security.UsuarioLogado;

@Service
public class ProblemaService {
    public record Foto(byte[] dados, String contentType) {
    }

    private static final Set<String> TIPOS_DE_FOTO = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long TAMANHO_MAXIMO_FOTO = 5L * 1024 * 1024;

    private final ProblemaRepository problemaRepository;
    private final FotoProblemaRepository fotoRepository;
    private final ApoioRepository apoioRepository;
    private final ObservacaoRepository observacaoRepository;
    private final ParticipacaoRepository participacaoRepository;
    private final ComunidadeService comunidadeService;
    private final ComunidadeRepository comunidadeRepository;
    private final UsuarioLogado usuarioLogado;
    private final ApplicationEventPublisher eventos;

    public ProblemaService(ProblemaRepository problemaRepository, FotoProblemaRepository fotoRepository,
            ApoioRepository apoioRepository, ObservacaoRepository observacaoRepository,
            ParticipacaoRepository participacaoRepository, ComunidadeService comunidadeService,
            ComunidadeRepository comunidadeRepository, UsuarioLogado usuarioLogado, ApplicationEventPublisher eventos) {
        this.problemaRepository = problemaRepository;
        this.fotoRepository = fotoRepository;
        this.apoioRepository = apoioRepository;
        this.observacaoRepository = observacaoRepository;
        this.participacaoRepository = participacaoRepository;
        this.comunidadeService = comunidadeService;
        this.comunidadeRepository = comunidadeRepository;
        this.usuarioLogado = usuarioLogado;
        this.eventos = eventos;
    }

    @Transactional
    public ProblemaResponse publicar(Long comunidadeId, PublicarProblemaRequest dados) {
        Usuario eu = usuarioLogado.usuario();
        Comunidade comunidade = comunidadeService.obter(comunidadeId);
        exigirMembro(comunidade, eu);

        Problema problema = new Problema();
        problema.setTitulo(dados.titulo().trim());
        problema.setDescricao(dados.descricao().trim());
        problema.setLatitude(Distancia.coordenada(dados.latitude()));
        problema.setLongitude(Distancia.coordenada(dados.longitude()));
        problema.setAutor(eu);
        problema.setComunidade(comunidade);
        problema.setAdotado(false);
        problemaRepository.save(problema);

        MultipartFile arquivo = dados.foto();
        boolean temFoto = arquivo != null && !arquivo.isEmpty();
        if (temFoto) {
            fotoRepository.save(lerFoto(arquivo, problema));
        }

        eventos.publishEvent(new ProblemaPublicadoEvento(problema.getId()));
        return ProblemaResponse.de(problema, 0, false, 0, temFoto, true, souGestor(comunidade, eu));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ProblemaResponse> feed(Long comunidadeId, String ordem, int pagina, int tamanho) {
        Usuario eu = usuarioLogado.usuario();
        Comunidade comunidade = comunidadeService.obter(comunidadeId);
        exigirMembro(comunidade, eu);

        String criterio = ordem == null || ordem.isBlank() ? "recentes" : ordem.trim().toLowerCase();
        if (criterio.equals("recentes")) {
            PageRequest paginacao = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "criadoEm"));
            return PaginaResponse.de(problemaRepository.findByComunidadeId(comunidadeId, paginacao).map(p -> resposta(p, eu)));
        }

        Map<Long, Long> apoios = new HashMap<>();
        for (Object[] linha : apoioRepository.contarPorProblemaDaComunidade(comunidadeId)) {
            apoios.put((Long) linha[0], ((Number) linha[1]).longValue());
        }
        Comparator<Problema> porApoios = Comparator.comparing((Problema p) -> apoios.getOrDefault(p.getId(), 0L)).reversed();
        Comparator<Problema> maisRecentes = Comparator.comparing(Problema::getCriadoEm).reversed();

        Comparator<Problema> comparador = switch (criterio) {
            case "apoiados" -> porApoios.thenComparing(maisRecentes);
            case "gravidade" -> Comparator.comparing(Problema::isAdotado)
                    .thenComparing(Comparator.comparing((Problema p) -> p.getGravidadeIa() == null ? -1 : p.getGravidadeIa().ordinal()).reversed())
                    .thenComparing(porApoios)
                    .thenComparing(maisRecentes);
            default -> throw new RegraNegocioException("Ordem inválida. Use recentes, apoiados ou gravidade.");
        };

        List<ProblemaResponse> ordenados = problemaRepository.findByComunidadeId(comunidadeId).stream()
                .sorted(comparador)
                .map(p -> resposta(p, eu))
                .toList();
        return PaginaResponse.de(ordenados, pagina, tamanho);
    }

    @Transactional(readOnly = true)
    public TriagemResponse triagem(int pagina, int tamanho) {
        Usuario eu = usuarioLogado.usuario();
        List<Long> comunidades = comunidadeRepository.findByGestorIdOrderByNomeAsc(eu.getId()).stream()
                .map(Comunidade::getId)
                .toList();
        List<Problema> pendentes = comunidades.isEmpty()
                ? List.of()
                : problemaRepository.findByComunidadeIdInAndAdotado(comunidades, false);

        List<ProblemaResponse> ordenados = pendentes.stream()
                .map(p -> resposta(p, eu))
                .sorted(Comparator.comparing((ProblemaResponse r) -> r.gravidadeIa() == null ? -1 : r.gravidadeIa().ordinal())
                        .reversed()
                        .thenComparing(Comparator.comparing(ProblemaResponse::totalApoios).reversed())
                        .thenComparing(Comparator.comparing(ProblemaResponse::criadoEm).reversed()))
                .toList();

        return new TriagemResponse(
                contar(pendentes, Gravidade.CRITICA),
                contar(pendentes, Gravidade.ALTA),
                contar(pendentes, Gravidade.MEDIA),
                contar(pendentes, Gravidade.BAIXA),
                contar(pendentes, null),
                PaginaResponse.de(ordenados, pagina, tamanho));
    }

    @Transactional
    public void reclassificar(Long id) {
        Usuario eu = usuarioLogado.usuario();
        Problema problema = obter(id);
        if (!souGestor(problema.getComunidade(), eu)) {
            throw new AcessoNegadoException("Só o gestor desta comunidade pode pedir uma nova análise da IA.");
        }
        eventos.publishEvent(new ProblemaPublicadoEvento(id, true));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ProblemaResponse> supervisionados(Long comunidadeId, int pagina, int tamanho) {
        Usuario eu = usuarioLogado.usuario();
        exigirMembro(comunidadeService.obter(comunidadeId), eu);
        PageRequest paginacao = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "adotadoEm"));
        return PaginaResponse.de(problemaRepository.findByComunidadeIdAndAdotado(comunidadeId, true, paginacao)
                .map(p -> resposta(p, eu)));
    }

    @Transactional(readOnly = true)
    public ProblemaResponse buscar(Long id) {
        Usuario eu = usuarioLogado.usuario();
        Problema problema = obter(id);
        exigirMembro(problema.getComunidade(), eu);
        return resposta(problema, eu);
    }

    @Transactional(readOnly = true)
    public Foto foto(Long id) {
        Usuario eu = usuarioLogado.usuario();
        exigirMembro(obter(id).getComunidade(), eu);
        FotoProblema foto = fotoRepository.findFirstByProblemaId(id)
                .orElseThrow(() -> new NaoEncontradoException("Este problema não tem foto."));
        return new Foto(foto.getDados(), foto.getContentType());
    }

    @Transactional
    public ProblemaResponse editar(Long id, EditarProblemaRequest dados) {
        Usuario eu = usuarioLogado.usuario();
        Problema problema = obter(id);
        exigirAutorAntesDaAdocao(problema, eu, "editar");
        problema.setTitulo(dados.titulo().trim());
        problema.setDescricao(dados.descricao().trim());
        return resposta(problema, eu);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario eu = usuarioLogado.usuario();
        Problema problema = obter(id);
        exigirAutorAntesDaAdocao(problema, eu, "excluir");
        problemaRepository.delete(problema);
    }

    @Transactional
    public ApoioResponse apoiar(Long id) {
        Usuario eu = usuarioLogado.usuario();
        Problema problema = obter(id);
        exigirMembro(problema.getComunidade(), eu);

        boolean apoiado = apoioRepository.findByUsuarioIdAndProblemaId(eu.getId(), id)
                .map(apoio -> {
                    apoioRepository.delete(apoio);
                    return false;
                })
                .orElseGet(() -> {
                    apoioRepository.save(new Apoio(eu, problema));
                    return true;
                });
        apoioRepository.flush();
        return new ApoioResponse(id, apoioRepository.countByProblemaId(id), apoiado);
    }

    @Transactional
    public ApoioResponse retirarApoio(Long id) {
        Usuario eu = usuarioLogado.usuario();
        Problema problema = obter(id);
        exigirMembro(problema.getComunidade(), eu);
        apoioRepository.findByUsuarioIdAndProblemaId(eu.getId(), id).ifPresent(apoioRepository::delete);
        apoioRepository.flush();
        return new ApoioResponse(id, apoioRepository.countByProblemaId(id), false);
    }

    @Transactional
    public ProblemaResponse adotar(Long id) {
        Usuario eu = usuarioLogado.usuario();
        Problema problema = obter(id);
        if (!souGestor(problema.getComunidade(), eu)) {
            throw new AcessoNegadoException("Só o gestor desta comunidade pode adotar o problema.");
        }
        if (problema.isAdotado()) {
            throw new ConflitoException("Este problema já foi adotado.");
        }
        problema.setAdotado(true);
        problema.setStatus(StatusProblema.PENDENTE);
        problema.setAdotadoEm(LocalDateTime.now());
        eventos.publishEvent(new ProblemaAdotadoEvento(id));
        return resposta(problema, eu);
    }

    @Transactional(readOnly = true)
    public List<ObservacaoResponse> observacoes(Long id) {
        Usuario eu = usuarioLogado.usuario();
        exigirMembro(obter(id).getComunidade(), eu);
        return observacaoRepository.findByProblemaIdOrderByCriadaEmAsc(id).stream()
                .map(o -> ObservacaoResponse.de(o, eu.getId()))
                .toList();
    }

    @Transactional
    public ObservacaoResponse observar(Long id, String texto) {
        Usuario eu = usuarioLogado.usuario();
        Problema problema = obter(id);
        exigirMembro(problema.getComunidade(), eu);

        Observacao observacao = new Observacao();
        observacao.setProblema(problema);
        observacao.setAutor(eu);
        observacao.setTexto(texto.trim());
        observacaoRepository.save(observacao);

        eventos.publishEvent(new ObservacaoAdicionadaEvento(id, observacao.getId(), eu.getId()));
        return ObservacaoResponse.de(observacao, eu.getId());
    }

    @Transactional
    public void excluirObservacao(Long observacaoId) {
        Usuario eu = usuarioLogado.usuario();
        Observacao observacao = observacaoRepository.findById(observacaoId)
                .orElseThrow(() -> new NaoEncontradoException("Observação não encontrada."));
        if (!observacao.getAutor().getId().equals(eu.getId())) {
            throw new AcessoNegadoException("Só quem escreveu a observação pode removê-la.");
        }
        observacaoRepository.delete(observacao);
    }

    private long contar(List<Problema> problemas, Gravidade gravidade) {
        return problemas.stream().filter(p -> p.getGravidadeIa() == gravidade).count();
    }

    private Problema obter(Long id) {
        return problemaRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Problema não encontrado."));
    }

    private boolean souGestor(Comunidade comunidade, Usuario eu) {
        return comunidade.getGestor().getId().equals(eu.getId());
    }

    private void exigirMembro(Comunidade comunidade, Usuario eu) {
        boolean membro = souGestor(comunidade, eu) || (eu.getPerfil() == Perfil.MORADOR
                && participacaoRepository.existsByUsuarioIdAndComunidadeId(eu.getId(), comunidade.getId()));
        if (!membro) {
            throw new AcessoNegadoException("Entre na comunidade para ver, publicar, apoiar ou comentar problemas.");
        }
    }

    private void exigirAutorAntesDaAdocao(Problema problema, Usuario eu, String acao) {
        if (!problema.getAutor().getId().equals(eu.getId())) {
            throw new AcessoNegadoException("Só quem publicou o problema pode " + acao + ".");
        }
        if (problema.isAdotado()) {
            throw new RegraNegocioException("O problema já foi adotado pelo gestor e não pode mais ser alterado.");
        }
    }

    private FotoProblema lerFoto(MultipartFile arquivo, Problema problema) {
        String tipo = arquivo.getContentType() == null ? "" : arquivo.getContentType().toLowerCase();
        if (!TIPOS_DE_FOTO.contains(tipo)) {
            throw new RegraNegocioException("A foto precisa ser JPG, PNG ou WEBP.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_FOTO) {
            throw new RegraNegocioException("A foto pode ter no máximo 5 MB.");
        }
        FotoProblema foto = new FotoProblema();
        foto.setProblema(problema);
        foto.setContentType(tipo);
        foto.setTamanhoBytes((int) arquivo.getSize());
        try {
            foto.setDados(arquivo.getBytes());
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível ler a foto enviada.");
        }
        return foto;
    }

    private ProblemaResponse resposta(Problema problema, Usuario eu) {
        Long id = problema.getId();
        return ProblemaResponse.de(problema,
                apoioRepository.countByProblemaId(id),
                apoioRepository.existsByUsuarioIdAndProblemaId(eu.getId(), id),
                observacaoRepository.countByProblemaId(id),
                fotoRepository.existsByProblemaId(id),
                problema.getAutor().getId().equals(eu.getId()),
                souGestor(problema.getComunidade(), eu));
    }
}
