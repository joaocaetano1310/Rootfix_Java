package br.com.fiap.rootfix.exception;

import org.springframework.http.HttpStatus;

public class NaoEncontradoException extends ApiException {
    public NaoEncontradoException(String mensagem) {
        super(HttpStatus.NOT_FOUND, mensagem);
    }
}
