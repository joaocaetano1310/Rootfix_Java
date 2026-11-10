package br.com.fiap.rootfix.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PublicadorDeEventos {
    private static final Logger log = LoggerFactory.getLogger(PublicadorDeEventos.class);

    private final RabbitTemplate rabbitTemplate;

    public PublicadorDeEventos(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener
    public void aoPublicarProblema(ProblemaPublicadoEvento evento) {
        enviar(RabbitConfig.CHAVE_PUBLICADO, evento);
    }

    @TransactionalEventListener
    public void aoAdicionarObservacao(ObservacaoAdicionadaEvento evento) {
        enviar(RabbitConfig.CHAVE_OBSERVACAO, evento);
    }

    @TransactionalEventListener
    public void aoAdotarProblema(ProblemaAdotadoEvento evento) {
        enviar(RabbitConfig.CHAVE_ADOTADO, evento);
    }

    private void enviar(String chave, Object evento) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, chave, evento);
            log.info("Evento {} enviado ao RabbitMQ: {}", chave, evento);
        } catch (AmqpException e) {
            log.warn("RabbitMQ indisponível, evento {} não enviado: {}", chave, e.getMessage());
        }
    }
}
