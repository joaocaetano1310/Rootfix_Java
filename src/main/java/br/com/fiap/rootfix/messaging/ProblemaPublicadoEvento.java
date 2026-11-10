package br.com.fiap.rootfix.messaging;

public record ProblemaPublicadoEvento(Long problemaId, Boolean somenteClassificar) {
    public ProblemaPublicadoEvento(Long problemaId) {
        this(problemaId, false);
    }

    public boolean apenasClassificacao() {
        return Boolean.TRUE.equals(somenteClassificar);
    }
}
