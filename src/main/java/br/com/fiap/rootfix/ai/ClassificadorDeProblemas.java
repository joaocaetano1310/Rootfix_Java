package br.com.fiap.rootfix.ai;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.MimeTypeUtils;

import br.com.fiap.rootfix.entity.Categoria;
import br.com.fiap.rootfix.entity.FotoProblema;
import br.com.fiap.rootfix.entity.Problema;
import br.com.fiap.rootfix.repository.CategoriaRepository;
import br.com.fiap.rootfix.repository.FotoProblemaRepository;
import br.com.fiap.rootfix.repository.ProblemaRepository;

@Service
public class ClassificadorDeProblemas {
    private static final Logger log = LoggerFactory.getLogger(ClassificadorDeProblemas.class);

    private static final String INSTRUCOES = """
            Você faz a triagem de problemas de zeladoria urbana relatados por moradores de um bairro em São Paulo.
            Você recebe o título, a descrição e uma foto do local. A gravidade deve ser avaliada pelo que aparece na foto:
            o texto serve para entender o que procurar, mas não basta sozinho para dar a gravidade.
            Responda com:
            - categoria: exatamente um dos nomes da lista de categorias informada;
            - fotoMostraOProblema: true se a foto mostra claramente o problema descrito; false se a foto não mostra,
              está escura, desfocada ou mostra outra coisa;
            - gravidade: só quando fotoMostraOProblema for true. BAIXA (incômodo, sem risco), MEDIA (atrapalha o dia a dia),
              ALTA (risco de acidente ou de saúde) ou CRITICA (risco imediato à vida ou à segurança, como fiação exposta,
              cratera em via movimentada ou alagamento). Quando fotoMostraOProblema for false, deixe gravidade vazia;
            - justificativa: uma frase curta em português, de até 200 caracteres, dizendo o que você viu na foto que justifica
              a gravidade ou, se a foto não mostrar o problema, por que não foi possível avaliar.
            """;

    private static final String SEM_FOTO = "Sem foto: não é possível avaliar a gravidade sem ver o local. Peça ao morador para adicionar uma foto.";

    private record Entrada(String titulo, String descricao, byte[] foto, String tipoDaFoto, List<String> categorias) {
    }

    private final ObjectProvider<ChatModel> chatModel;
    private final ProblemaRepository problemaRepository;
    private final FotoProblemaRepository fotoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TransactionTemplate transacao;

    public ClassificadorDeProblemas(ObjectProvider<ChatModel> chatModel, ProblemaRepository problemaRepository,
            FotoProblemaRepository fotoRepository, CategoriaRepository categoriaRepository,
            TransactionTemplate transacao) {
        this.chatModel = chatModel;
        this.problemaRepository = problemaRepository;
        this.fotoRepository = fotoRepository;
        this.categoriaRepository = categoriaRepository;
        this.transacao = transacao;
    }

    public void classificar(Long problemaId) {
        ChatModel modelo = chatModel.getIfAvailable();
        if (modelo == null) {
            log.info("IA desativada (IA_PROVEDOR=none): problema {} fica sem classificação.", problemaId);
            return;
        }

        Entrada entrada = transacao.execute(status -> problemaRepository.findById(problemaId).map(p -> {
            FotoProblema foto = fotoRepository.findFirstByProblemaId(problemaId).orElse(null);
            return new Entrada(p.getTitulo(), p.getDescricao(), foto == null ? null : foto.getDados(),
                    foto == null ? null : foto.getContentType(),
                    categoriaRepository.findAllByOrderByNomeAsc().stream().map(Categoria::getNome).toList());
        }).orElse(null));
        if (entrada == null) {
            return;
        }
        if (entrada.foto() == null) {
            transacao.executeWithoutResult(status -> problemaRepository.findById(problemaId).ifPresent(p -> {
                p.setGravidadeIa(null);
                p.setJustificativaIa(SEM_FOTO);
            }));
            log.info("Problema {} sem foto: a IA não avalia a gravidade.", problemaId);
            return;
        }

        ClassificacaoIA resultado;
        try {
            String texto = "Categorias possíveis: " + String.join(", ", entrada.categorias())
                    + "\nTítulo: " + entrada.titulo()
                    + "\nDescrição: " + entrada.descricao()
                    + "\nA foto do local está anexada.";
            resultado = ChatClient.create(modelo).prompt()
                    .system(INSTRUCOES)
                    .user(u -> u.text(texto)
                            .media(MimeTypeUtils.parseMimeType(entrada.tipoDaFoto()), new ByteArrayResource(entrada.foto())))
                    .call()
                    .entity(ClassificacaoIA.class);
        } catch (Exception e) {
            log.warn("A IA não conseguiu classificar o problema {}: {}", problemaId, e.getMessage());
            return;
        }

        if (resultado == null) {
            log.warn("A IA devolveu uma resposta vazia para o problema {}.", problemaId);
            return;
        }

        transacao.executeWithoutResult(status -> problemaRepository.findById(problemaId).ifPresent(p -> aplicar(p, resultado)));
        log.info("Problema {} classificado pela IA: {} / {} (foto mostra o problema: {})", problemaId,
                resultado.categoria(), resultado.gravidade(), resultado.fotoMostraOProblema());
    }

    private void aplicar(Problema problema, ClassificacaoIA resultado) {
        List<Categoria> categorias = categoriaRepository.findAllByOrderByNomeAsc();
        Categoria categoria = categorias.stream()
                .filter(c -> resultado.categoria() != null && c.getNome().equalsIgnoreCase(resultado.categoria().trim()))
                .findFirst()
                .orElseGet(() -> categorias.stream().filter(c -> c.getNome().equalsIgnoreCase("Outros")).findFirst().orElse(null));

        String justificativa = resultado.justificativa() == null ? null : resultado.justificativa().trim();
        if (justificativa != null && justificativa.length() > 500) {
            justificativa = justificativa.substring(0, 497) + "...";
        }

        problema.setCategoria(categoria);
        problema.setGravidadeIa(resultado.fotoMostraOProblema() ? resultado.gravidade() : null);
        problema.setJustificativaIa(justificativa);
    }
}
