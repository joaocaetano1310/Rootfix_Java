package br.com.fiap.rootfix.dto;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublicarProblemaRequest(
        @NotBlank(message = "Informe o título.")
        @Size(max = 120, message = "O título pode ter no máximo 120 caracteres.")
        String titulo,

        @NotBlank(message = "Descreva o problema.")
        @Size(max = 1000, message = "A descrição pode ter no máximo 1000 caracteres.")
        String descricao,

        @NotNull(message = "Informe a latitude.")
        @DecimalMin(value = "-90", message = "Latitude inválida.")
        @DecimalMax(value = "90", message = "Latitude inválida.")
        BigDecimal latitude,

        @NotNull(message = "Informe a longitude.")
        @DecimalMin(value = "-180", message = "Longitude inválida.")
        @DecimalMax(value = "180", message = "Longitude inválida.")
        BigDecimal longitude,

        MultipartFile foto) {
}
