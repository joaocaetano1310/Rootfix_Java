package br.com.fiap.rootfix.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.rootfix.dto.ComunicadoRequest;
import br.com.fiap.rootfix.dto.ComunicadoResponse;
import br.com.fiap.rootfix.dto.PaginaResponse;
import br.com.fiap.rootfix.entity.Comunicado;
import br.com.fiap.rootfix.entity.Comunidade;
import br.com.fiap.rootfix.entity.Perfil;
import br.com.fiap.rootfix.entity.Usuario;
import br.com.fiap.rootfix.exception.AcessoNegadoException;
import br.com.fiap.rootfix.exception.NaoEncontradoException;
import br.com.fiap.rootfix.messaging.ComunicadoPublicadoEvento;
import br.com.fiap.rootfix.repository.ComunicadoRepository;
import br.com.fiap.rootfix.repository.ParticipacaoRepository;
import br.com.fiap.rootfix.security.UsuarioLogado;

@Service
public class ComunicadoService {
    private final ComunicadoRepository comunicadoRepository;
    private final ParticipacaoRepository participacaoRepository;
    private final ComunidadeService comunidadeService;
    private final UsuarioLogado usuarioLogado;
    private final ApplicationEventPublisher eventos;

    public ComunicadoService(ComunicadoRepository comunicadoRepository, ParticipacaoRepository participacaoRepository,
            ComunidadeService comunidadeService, UsuarioLogado usuarioLogado, ApplicationEventPublisher eventos) {
        this.comunicadoRepository = comunicadoRepository;
        this.participacaoRepository = participacaoRepository;
        this.comunidadeService = comunidadeService;
        this.usuarioLogado = usuarioLogado;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ComunicadoResponse> listar(Long comunidadeId, int pagina, int tamanho) {
        Usuario eu = usuarioLogado.usuario();
        Comunidade comunidade = comunidadeService.obter(comunidadeId);
        exigirMembro(comunidade, eu);
        return PaginaResponse.de(comunicadoRepository
                .findByComunidadeIdOrderByDestacadoDescCriadoEmDesc(comunidadeId, PageRequest.of(pagina, tamanho))
                .map(c -> ComunicadoResponse.de(c, eu.getId())));
    }

    @Transactional
    public ComunicadoResponse publicar(Long comunidadeId, ComunicadoRequest dados) {
        Usuario eu = usuarioLogado.usuario();
        Comunidade comunidade = comunidadeService.obter(comunidadeId);
        exigirGestorDono(comunidade, eu);

        Comunicado comunicado = new Comunicado();
        comunicado.setComunidade(comunidade);
        comunicado.setAutor(eu);
        preencher(comunicado, dados);
        comunicadoRepository.save(comunicado);

        eventos.publishEvent(new ComunicadoPublicadoEvento(comunicado.getId()));
        return ComunicadoResponse.de(comunicado, eu.getId());
    }

    @Transactional
    public ComunicadoResponse editar(Long id, ComunicadoRequest dados) {
        Usuario eu = usuarioLogado.usuario();
        Comunicado comunicado = obter(id);
        exigirGestorDono(comunicado.getComunidade(), eu);
        preencher(comunicado, dados);
        return ComunicadoResponse.de(comunicado, eu.getId());
    }

    @Transactional
    public void excluir(Long id) {
        Comunicado comunicado = obter(id);
        exigirGestorDono(comunicado.getComunidade(), usuarioLogado.usuario());
        comunicadoRepository.delete(comunicado);
    }

    private Comunicado obter(Long id) {
        return comunicadoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Comunicado não encontrado."));
    }

    private void preencher(Comunicado comunicado, ComunicadoRequest dados) {
        comunicado.setTitulo(dados.titulo().trim());
        comunicado.setTexto(dados.texto().trim());
        comunicado.setDestacado(dados.emDestaque());
    }

    private void exigirGestorDono(Comunidade comunidade, Usuario eu) {
        if (!comunidade.getGestor().getId().equals(eu.getId())) {
            throw new AcessoNegadoException("Só o gestor desta comunidade pode publicar e alterar comunicados.");
        }
    }

    private void exigirMembro(Comunidade comunidade, Usuario eu) {
        boolean membro = comunidade.getGestor().getId().equals(eu.getId()) || (eu.getPerfil() == Perfil.MORADOR
                && participacaoRepository.existsByUsuarioIdAndComunidadeId(eu.getId(), comunidade.getId()));
        if (!membro) {
            throw new AcessoNegadoException("Entre na comunidade para ver os comunicados.");
        }
    }
}
