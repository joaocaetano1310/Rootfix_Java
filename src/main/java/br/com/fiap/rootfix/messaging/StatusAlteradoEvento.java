package br.com.fiap.rootfix.messaging;

public record StatusAlteradoEvento(Long problemaId, String statusAnterior, String statusNovo, String observacao) {
    public StatusAlteradoEvento(Long problemaId, String statusAnterior, String statusNovo) {
        this(problemaId, statusAnterior, statusNovo, null);
    }

    public boolean temObservacao() {
        return observacao != null && !observacao.isBlank();
    }
}
