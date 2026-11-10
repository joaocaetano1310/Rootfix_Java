package br.com.fiap.rootfix.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.rootfix.entity.Comunidade;

public record ComunidadeResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal latitude,
        BigDecimal longitude,
        GestorResumo gestor,
        long totalMembros,
        Double distanciaKm,
        boolean participo,
        boolean souGestor,
        LocalDateTime criadaEm) {
    public static ComunidadeResponse de(Comunidade c, long totalMembros, Double distanciaKm,
            boolean participo, boolean souGestor) {
        return new ComunidadeResponse(c.getId(), c.getNome(), c.getDescricao(), c.getLatitude(),
                c.getLongitude(), GestorResumo.de(c.getGestor()), totalMembros, distanciaKm,
                participo, souGestor, c.getCriadaEm());
    }
}
