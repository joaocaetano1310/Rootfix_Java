package br.com.fiap.rootfix.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "rootfix.eventos";

    public static final String CHAVE_PUBLICADO = "problema.publicado";
    public static final String CHAVE_OBSERVACAO = "observacao.adicionada";
    public static final String CHAVE_ADOTADO = "problema.adotado";
    public static final String CHAVE_STATUS = "problema.status-alterado";

    public static final String FILA_PUBLICADO = "rootfix.problema.publicado";
    public static final String FILA_OBSERVACAO = "rootfix.observacao.adicionada";
    public static final String FILA_ADOTADO = "rootfix.problema.adotado";
    public static final String FILA_STATUS = "rootfix.problema.status-alterado";

    @Bean
    public TopicExchange exchangeDeEventos() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue filaPublicado() {
        return QueueBuilder.durable(FILA_PUBLICADO).build();
    }

    @Bean
    public Queue filaObservacao() {
        return QueueBuilder.durable(FILA_OBSERVACAO).build();
    }

    @Bean
    public Queue filaAdotado() {
        return QueueBuilder.durable(FILA_ADOTADO).build();
    }

    @Bean
    public Queue filaStatus() {
        return QueueBuilder.durable(FILA_STATUS).build();
    }

    @Bean
    public Binding ligacaoPublicado(TopicExchange exchangeDeEventos, Queue filaPublicado) {
        return BindingBuilder.bind(filaPublicado).to(exchangeDeEventos).with(CHAVE_PUBLICADO);
    }

    @Bean
    public Binding ligacaoObservacao(TopicExchange exchangeDeEventos, Queue filaObservacao) {
        return BindingBuilder.bind(filaObservacao).to(exchangeDeEventos).with(CHAVE_OBSERVACAO);
    }

    @Bean
    public Binding ligacaoAdotado(TopicExchange exchangeDeEventos, Queue filaAdotado) {
        return BindingBuilder.bind(filaAdotado).to(exchangeDeEventos).with(CHAVE_ADOTADO);
    }

    @Bean
    public Binding ligacaoStatus(TopicExchange exchangeDeEventos, Queue filaStatus) {
        return BindingBuilder.bind(filaStatus).to(exchangeDeEventos).with(CHAVE_STATUS);
    }

    @Bean
    public MessageConverter conversorJson() {
        JacksonJsonMessageConverter conversor = new JacksonJsonMessageConverter("br.com.fiap.rootfix.messaging");
        conversor.setAlwaysConvertToInferredType(true);
        return conversor;
    }
}
