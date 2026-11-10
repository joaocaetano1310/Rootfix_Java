package br.com.fiap.rootfix.exception;

import org.springframework.http.HttpStatus;

public class CredenciaisInvalidasException extends ApiException {
    public CredenciaisInvalidasException(String mensagem) {
        super(HttpStatus.UNAUTHORIZED, mensagem);
    }
}
