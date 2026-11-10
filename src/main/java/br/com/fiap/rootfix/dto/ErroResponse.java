package br.com.fiap.rootfix.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        String caminho,
        LocalDateTime timestamp,
        List<CampoErro> campos) {
    public static ErroResponse de(HttpStatus status, String mensagem, String caminho, List<CampoErro> campos) {
        return new ErroResponse(status.value(), status.getReasonPhrase(), mensagem, caminho,
                LocalDateTime.now(), campos);
    }
}
