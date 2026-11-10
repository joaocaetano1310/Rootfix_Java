package br.com.fiap.rootfix.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComunidadeRequest(
        @NotBlank(message = "Informe o nome da comunidade.")
        @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres.")
        String nome,

        @Size(max = 500, message = "A descrição pode ter no máximo 500 caracteres.")
        String descricao,

        @NotNull(message = "Informe a latitude.")
        @DecimalMin(value = "-90", message = "Latitude inválida.")
        @DecimalMax(value = "90", message = "Latitude inválida.")
        BigDecimal latitude,

        @NotNull(message = "Informe a longitude.")
        @DecimalMin(value = "-180", message = "Longitude inválida.")
        @DecimalMax(value = "180", message = "Longitude inválida.")
        BigDecimal longitude) {
}
