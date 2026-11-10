package br.com.fiap.rootfix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditarProblemaRequest(
        @NotBlank(message = "Informe o título.")
        @Size(max = 120, message = "O título pode ter no máximo 120 caracteres.")
        String titulo,

        @NotBlank(message = "Descreva o problema.")
        @Size(max = 1000, message = "A descrição pode ter no máximo 1000 caracteres.")
        String descricao) {
}
