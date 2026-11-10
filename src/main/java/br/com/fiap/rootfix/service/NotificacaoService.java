package br.com.fiap.rootfix.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.rootfix.dto.NotificacaoResponse;
import br.com.fiap.rootfix.dto.PaginaResponse;
import br.com.fiap.rootfix.entity.Notificacao;
import br.com.fiap.rootfix.entity.PrioridadeNotificacao;
import br.com.fiap.rootfix.entity.Problema;
import br.com.fiap.rootfix.entity.StatusProblema;
import br.com.fiap.rootfix.exception.NaoEncontradoException;
import br.com.fiap.rootfix.messaging.ObservacaoAdicionadaEvento;
import br.com.fiap.rootfix.messaging.StatusAlteradoEvento;
import br.com.fiap.rootfix.repository.ApoioRepository;
import br.com.fiap.rootfix.repository.ComunicadoRepository;
import br.com.fiap.rootfix.repository.NotificacaoRepository;
import br.com.fiap.rootfix.repository.ObservacaoRepository;
import br.com.fiap.rootfix.repository.ParticipacaoRepository;
import br.com.fiap.rootfix.repository.ProblemaRepository;
import br.com.fiap.rootfix.repository.UsuarioRepository;
import br.com.fiap.rootfix.security.UsuarioLogado;

@Service
public class NotificacaoService {
    private static final Map<String, String> NOME_DO_STATUS = Map.of(
            StatusProblema.PENDENTE.name(), "Pendente",
            StatusProblema.EM_ANDAMENTO.name(), "Em andamento",
            StatusProblema.RESOLVIDO.name(), "Resolvido",
            StatusProblema.ARQUIVADO.name(), "Arquivado");

    private final NotificacaoRepository notificacaoRepository;
    private final ProblemaRepository problemaRepository;
    private final ObservacaoRepository observacaoRepository;
    private final ApoioRepository apoioRepository;
    private final ParticipacaoRepository participacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComunicadoRepository comunicadoRepository;
    private final UsuarioLogado usuarioLogado;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, ProblemaRepository problemaRepository,
            ObservacaoRepository observacaoRepository, ApoioRepository apoioRepository,
            ParticipacaoRepository participacaoRepository, UsuarioRepository usuarioRepository,
            ComunicadoRepository comunicadoRepository, UsuarioLogado usuarioLogado) {
        this.notificacaoRepository = notificacaoRepository;
        this.problemaRepository = problemaRepository;
        this.observacaoRepository = observacaoRepository;
        this.apoioRepository = apoioRepository;
        this.participacaoRepository = participacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.comunicadoRepository = comunicadoRepository;
        this.usuarioLogado = usuarioLogado;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<NotificacaoResponse> minhas(int pagina, int tamanho) {
        return PaginaResponse.de(notificacaoRepository
                .findByUsuarioIdOrderByCriadaEmDesc(usuarioLogado.id(), PageRequest.of(pagina, tamanho))
                .map(NotificacaoResponse::de));
    }

    @Transactional
    public NotificacaoResponse marcarComoLida(Long id) {
        Notificacao notificacao = notificacaoRepository.findByIdAndUsuarioId(id, usuarioLogado.id())
                .orElseThrow(() -> new NaoEncontradoException("Notificação não encontrada."));
        notificacao.setLida(true);
        return NotificacaoResponse.de(notificacao);
    }

    @Transactional
    public void excluir(Long id) {
        Notificacao notificacao = notificacaoRepository.findByIdAndUsuarioId(id, usuarioLogado.id())
                .orElseThrow(() -> new NaoEncontradoException("Notificação não encontrada."));
        notificacaoRepository.delete(notificacao);
    }

    @Transactional
    public long excluirLidas() {
        return notificacaoRepository.deleteByUsuarioIdAndLida(usuarioLogado.id(), true);
    }

    @Transactional
    public void problemaPublicado(Long problemaId) {
        problemaRepository.findById(problemaId).ifPresent(problema -> {
            Long gestorId = problema.getComunidade().getGestor().getId();
            if (!gestorId.equals(problema.getAutor().getId())) {
                criar(gestorId, problema, "Novo problema na " + problema.getComunidade().getNome() + ": \""
                        + problema.getTitulo() + "\", publicado por " + problema.getAutor().getNome() + ".",
                        PrioridadeNotificacao.ALTA);
            }
        });
    }

    @Transactional
    public void observacaoAdicionada(ObservacaoAdicionadaEvento evento) {
        problemaRepository.findById(evento.problemaId()).ifPresent(problema -> {
            if (!problema.getAutor().getId().equals(evento.autorId())) {
                criar(problema.getAutor().getId(), problema,
                        "Nova observação no seu problema \"" + problema.getTitulo() + "\".", PrioridadeNotificacao.ALTA);
            }
        });
    }

    @Transactional
    public void problemaAdotado(Long problemaId) {
        problemaRepository.findById(problemaId).ifPresent(problema -> notificarEnvolvidos(problema,
                "O gestor adotou o problema \"" + problema.getTitulo() + "\". Status: Pendente."));
    }

    @Transactional
    public void statusAlterado(StatusAlteradoEvento evento) {
        problemaRepository.findById(evento.problemaId()).ifPresent(problema -> {
            String status = NOME_DO_STATUS.getOrDefault(evento.statusNovo(), evento.statusNovo());
            notificarEnvolvidos(problema, "O problema \"" + problema.getTitulo() + "\" mudou para " + status + ".");
        });
    }

    @Transactional
    public void comunicadoPublicado(Long comunicadoId) {
        comunicadoRepository.findById(comunicadoId).ifPresent(comunicado -> {
            PrioridadeNotificacao prioridade = comunicado.isDestacado()
                    ? PrioridadeNotificacao.ALTA
                    : PrioridadeNotificacao.BAIXA;
            String mensagem = (comunicado.isDestacado() ? "Comunicado em destaque" : "Novo comunicado")
                    + " na " + comunicado.getComunidade().getNome() + ": \"" + comunicado.getTitulo() + "\".";
            participacaoRepository.findByComunidadeId(comunicado.getComunidade().getId())
                    .forEach(p -> criar(p.getUsuario().getId(), null, mensagem, prioridade));
        });
    }

    private void notificarEnvolvidos(Problema problema, String mensagem) {
        Map<Long, PrioridadeNotificacao> destinatarios = new LinkedHashMap<>();
        destinatarios.put(problema.getAutor().getId(), PrioridadeNotificacao.ALTA);
        observacaoRepository.findByProblemaIdOrderByCriadaEmAsc(problema.getId())
                .forEach(o -> destinatarios.put(o.getAutor().getId(), PrioridadeNotificacao.ALTA));
        apoioRepository.findByProblemaId(problema.getId())
                .forEach(a -> destinatarios.putIfAbsent(a.getUsuario().getId(), PrioridadeNotificacao.BAIXA));
        participacaoRepository.findByComunidadeId(problema.getComunidade().getId())
                .forEach(p -> destinatarios.putIfAbsent(p.getUsuario().getId(), PrioridadeNotificacao.BAIXA));
        destinatarios.remove(problema.getComunidade().getGestor().getId());

        destinatarios.forEach((usuarioId, prioridade) -> criar(usuarioId, problema, mensagem, prioridade));
    }

    private void criar(Long usuarioId, Problema problema, String mensagem, PrioridadeNotificacao prioridade) {
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        notificacao.setProblema(problema);
        notificacao.setMensagem(mensagem.length() > 300 ? mensagem.substring(0, 297) + "..." : mensagem);
        notificacao.setPrioridade(prioridade);
        notificacao.setLida(false);
        notificacaoRepository.save(notificacao);
    }
}
