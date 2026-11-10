package br.com.fiap.rootfix.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.com.fiap.rootfix.ai.ClassificadorDeProblemas;
import br.com.fiap.rootfix.service.NotificacaoService;

@Component
public class OuvinteDeEventos {
    private static final Logger log = LoggerFactory.getLogger(OuvinteDeEventos.class);

    private final ClassificadorDeProblemas classificador;
    private final NotificacaoService notificacaoService;

    public OuvinteDeEventos(ClassificadorDeProblemas classificador, NotificacaoService notificacaoService) {
        this.classificador = classificador;
        this.notificacaoService = notificacaoService;
    }

    @RabbitListener(queues = RabbitConfig.FILA_PUBLICADO)
    public void problemaPublicado(ProblemaPublicadoEvento evento) {
        log.info("Recebido problema.publicado: {}", evento);
        if (!evento.apenasClassificacao()) {
            notificacaoService.problemaPublicado(evento.problemaId());
        }
        classificador.classificar(evento.problemaId());
    }

    @RabbitListener(queues = RabbitConfig.FILA_OBSERVACAO)
    public void observacaoAdicionada(ObservacaoAdicionadaEvento evento) {
        log.info("Recebido observacao.adicionada: {}", evento);
        notificacaoService.observacaoAdicionada(evento);
    }

    @RabbitListener(queues = RabbitConfig.FILA_ADOTADO)
    public void problemaAdotado(ProblemaAdotadoEvento evento) {
        log.info("Recebido problema.adotado: {}", evento);
        notificacaoService.problemaAdotado(evento.problemaId());
    }

    @RabbitListener(queues = RabbitConfig.FILA_STATUS)
    public void statusAlterado(StatusAlteradoEvento evento) {
        log.info("Recebido problema.status-alterado: {}", evento);
        notificacaoService.statusAlterado(evento);
    }
}
