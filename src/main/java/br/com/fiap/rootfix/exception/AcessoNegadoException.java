package br.com.fiap.rootfix.exception;

import org.springframework.http.HttpStatus;

public class AcessoNegadoException extends ApiException {
    public AcessoNegadoException(String mensagem) {
        super(HttpStatus.FORBIDDEN, mensagem);
    }
}
