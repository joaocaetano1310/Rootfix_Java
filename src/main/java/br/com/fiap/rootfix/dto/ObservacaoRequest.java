package br.com.fiap.rootfix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ObservacaoRequest(
        @NotBlank(message = "Escreva a observação.")
        @Size(max = 1000, message = "A observação pode ter no máximo 1000 caracteres.")
        String texto) {
}
