package br.com.fiap.rootfix.dto;

import java.time.LocalDateTime;

import br.com.fiap.rootfix.entity.Notificacao;
import br.com.fiap.rootfix.entity.PrioridadeNotificacao;

public record NotificacaoResponse(Long id, String mensagem, PrioridadeNotificacao prioridade, boolean lida,
        LocalDateTime criadaEm, Long problemaId) {
    public static NotificacaoResponse de(Notificacao n) {
        return new NotificacaoResponse(n.getId(), n.getMensagem(), n.getPrioridade(), n.isLida(), n.getCriadaEm(),
                n.getProblema() == null ? null : n.getProblema().getId());
    }
}
