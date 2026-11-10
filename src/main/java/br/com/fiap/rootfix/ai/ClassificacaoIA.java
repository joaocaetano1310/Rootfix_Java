package br.com.fiap.rootfix.ai;

import br.com.fiap.rootfix.entity.Gravidade;

public record ClassificacaoIA(String categoria, boolean fotoMostraOProblema, Gravidade gravidade, String justificativa) {
}
