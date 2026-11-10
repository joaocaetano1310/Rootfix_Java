package br.com.fiap.rootfix.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroMoradorRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 120, message = "O nome pode ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "O e-mail pode ter no máximo 150 caracteres.")
        String email,

        @NotBlank(message = "Informe a senha.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String senha,

        @NotBlank(message = "Informe o endereço.")
        @Size(max = 255, message = "O endereço pode ter no máximo 255 caracteres.")
        String endereco,

        @NotNull(message = "Informe a latitude.")
        @DecimalMin(value = "-90", message = "Latitude inválida.")
        @DecimalMax(value = "90", message = "Latitude inválida.")
        BigDecimal latitude,

        @NotNull(message = "Informe a longitude.")
        @DecimalMin(value = "-180", message = "Longitude inválida.")
        @DecimalMax(value = "180", message = "Longitude inválida.")
        BigDecimal longitude) {
}
