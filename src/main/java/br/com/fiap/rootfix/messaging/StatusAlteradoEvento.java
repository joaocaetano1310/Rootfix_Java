package br.com.fiap.rootfix.messaging;

public record StatusAlteradoEvento(Long problemaId, String statusAnterior, String statusNovo) {
}
