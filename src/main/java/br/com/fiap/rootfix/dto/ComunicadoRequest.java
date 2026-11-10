package br.com.fiap.rootfix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComunicadoRequest(
        @NotBlank(message = "Informe o título do comunicado.")
        @Size(max = 120, message = "O título pode ter no máximo 120 caracteres.")
        String titulo,

        @NotBlank(message = "Escreva o texto do comunicado.")
        @Size(max = 2000, message = "O texto pode ter no máximo 2000 caracteres.")
        String texto,

        Boolean destacado) {

    public boolean emDestaque() {
        return Boolean.TRUE.equals(destacado);
    }
}
