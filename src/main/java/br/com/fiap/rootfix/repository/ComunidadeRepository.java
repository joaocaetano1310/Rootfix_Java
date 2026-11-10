package br.com.fiap.rootfix.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.rootfix.entity.Comunidade;

public interface ComunidadeRepository extends JpaRepository<Comunidade, Long> {
    List<Comunidade> findByGestorIdOrderByNomeAsc(Long gestorId);

    List<Comunidade> findByLatitudeBetweenAndLongitudeBetween(BigDecimal latitudeMinima, BigDecimal latitudeMaxima,
            BigDecimal longitudeMinima, BigDecimal longitudeMaxima);
}
